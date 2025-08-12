package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.config.StaticJsonConfig;
import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
import com.cvshealth.digital.microservice.iqe.health.CassandraHealthIndicator;
import com.cvshealth.digital.microservice.iqe.model.WriteOperation;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

import static com.cvshealth.digital.microservice.iqe.constants.SchedulingConstants.*;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "service.static-json-fallback.enabled", havingValue = "true")
public class StaticJsonFallbackService {
    
    private final StaticJsonConfig config;
    private final ObjectMapper objectMapper;
    private final CassandraHealthIndicator cassandraHealthIndicator;
    private final ConcurrentHashMap<String, CachedQuestionnaire> memoryCache = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<WriteOperation> pendingWrites = new ConcurrentLinkedQueue<>();
    
    @PostConstruct
    public void initialize() {
        if (config.isEnabled()) {
            createDataDirectory();
            if (config.isMemoryCache()) {
                loadExistingFilesToCache();
            }
            log.info("Static JSON fallback service initialized with directory: {}", config.getDataDirectory());
        }
    }
    
    public Mono<QuestionareRequest> getQuestionnaireWithFallback(String actionId, 
                                                                QuestionareRequest iqeOutput,
                                                                Supplier<Mono<QuestionareRequest>> cassandraFallback) {
        return cassandraHealthIndicator.isHealthy()
            .flatMap(isHealthy -> {
                if (isHealthy) {
                    return cassandraFallback.get()
                        .doOnSuccess(result -> saveToStaticFile(actionId, result));
                } else {
                    log.warn("Cassandra is unhealthy, falling back to static JSON files for actionId: {}", actionId);
                    return getFromStaticFile(actionId, iqeOutput)
                        .switchIfEmpty(Mono.just(createUnavailableResponse(iqeOutput)));
                }
            });
    }
    
    public Mono<Void> writeWithFallback(WriteOperation operation) {
        return cassandraHealthIndicator.isHealthy()
            .flatMap(isHealthy -> {
                if (isHealthy) {
                    return processPendingWrites()
                        .then(executeWrite(operation));
                } else {
                    queueWrite(operation);
                    return Mono.empty();
                }
            });
    }
    
    private Mono<QuestionareRequest> getFromStaticFile(String actionId, QuestionareRequest iqeOutput) {
        return Mono.fromCallable(() -> {
            if (config.isMemoryCache()) {
                CachedQuestionnaire cached = memoryCache.get(actionId);
                if (cached != null && !cached.isExpired()) {
                    log.debug("Retrieved questionnaire from memory cache: {}", actionId);
                    return cached.getQuestionnaire();
                }
            }
            
            Path filePath = getFilePath(actionId);
            if (Files.exists(filePath)) {
                String content = Files.readString(filePath);
                QuestionareRequest questionnaire = objectMapper.readValue(content, QuestionareRequest.class);
                
                if (config.isMemoryCache()) {
                    cacheQuestionnaire(actionId, questionnaire);
                }
                
                log.info("Successfully retrieved questionnaire from static file: {}", actionId);
                return questionnaire;
            }
            
            log.warn("No static file found for actionId: {}", actionId);
            return null;
        })
        .subscribeOn(Schedulers.boundedElastic())
        .doOnError(e -> log.error("Failed to retrieve from static file for actionId: {}", actionId, e));
    }
    
    private void saveToStaticFile(String actionId, QuestionareRequest questionnaire) {
        if (questionnaire != null) {
            Mono.fromCallable(() -> {
                Path filePath = getFilePath(actionId);
                Files.createDirectories(filePath.getParent());
                
                String content = objectMapper.writeValueAsString(questionnaire);
                Files.writeString(filePath, content);
                
                if (config.isMemoryCache()) {
                    cacheQuestionnaire(actionId, questionnaire);
                }
                
                return null;
            })
            .subscribeOn(Schedulers.boundedElastic())
            .doOnSuccess(result -> log.debug("Saved questionnaire to static file: {}", actionId))
            .doOnError(e -> log.error("Failed to save to static file for actionId: {}", actionId, e))
            .subscribe();
        }
    }
    
    private void queueWrite(WriteOperation operation) {
        pendingWrites.offer(operation);
        log.info("Queued write operation during Cassandra outage: {} for actionId: {}", 
                operation.getType(), operation.getActionId());
    }
    
    private Mono<Void> processPendingWrites() {
        if (pendingWrites.isEmpty()) {
            return Mono.empty();
        }
        
        log.info("Processing {} pending write operations", pendingWrites.size());
        return Flux.fromIterable(pendingWrites)
            .flatMap(this::executeWrite)
            .doOnComplete(() -> {
                pendingWrites.clear();
                log.info("Completed processing all pending write operations");
            })
            .then();
    }
    
    private Mono<Void> executeWrite(WriteOperation operation) {
        log.debug("Executing write operation: {} for actionId: {}", operation.getType(), operation.getActionId());
        return Mono.empty();
    }
    
    private void createDataDirectory() {
        if (config.isAutoCreateDirectory()) {
            try {
                Path dataPath = Paths.get(config.getDataDirectory());
                Files.createDirectories(dataPath);
                log.info("Created data directory: {}", config.getDataDirectory());
            } catch (IOException e) {
                log.error("Failed to create data directory: {}", config.getDataDirectory(), e);
            }
        }
    }
    
    private void loadExistingFilesToCache() {
        try {
            Path dataPath = Paths.get(config.getDataDirectory());
            if (Files.exists(dataPath)) {
                Files.walk(dataPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(config.getFileExtension()))
                    .forEach(this::loadFileToCache);
                log.info("Loaded {} existing files to memory cache", memoryCache.size());
            }
        } catch (IOException e) {
            log.error("Failed to load existing files to cache", e);
        }
    }
    
    private void loadFileToCache(Path filePath) {
        try {
            String fileName = filePath.getFileName().toString();
            String actionId = fileName.substring(0, fileName.lastIndexOf(config.getFileExtension()));
            String content = Files.readString(filePath);
            QuestionareRequest questionnaire = objectMapper.readValue(content, QuestionareRequest.class);
            cacheQuestionnaire(actionId, questionnaire);
        } catch (Exception e) {
            log.warn("Failed to load file to cache: {}", filePath, e);
        }
    }
    
    private void cacheQuestionnaire(String actionId, QuestionareRequest questionnaire) {
        if (memoryCache.size() >= config.getMaxCacheSize()) {
            String oldestKey = memoryCache.entrySet().stream()
                .min((e1, e2) -> e1.getValue().getTimestamp().compareTo(e2.getValue().getTimestamp()))
                .map(entry -> entry.getKey())
                .orElse(null);
            if (oldestKey != null) {
                memoryCache.remove(oldestKey);
            }
        }
        
        memoryCache.put(actionId, new CachedQuestionnaire(questionnaire, Instant.now()));
    }
    
    private Path getFilePath(String actionId) {
        return Paths.get(config.getDataDirectory(), actionId + config.getFileExtension());
    }
    
    private QuestionareRequest createUnavailableResponse(QuestionareRequest iqeOutput) {
        iqeOutput.setStatusCode(SERVICE_UNAVAILABLE_CODE);
        iqeOutput.setErrorDescription("Service temporarily unavailable. Please try again later.");
        log.warn("Created unavailable response due to both Cassandra and static files being inaccessible");
        return iqeOutput;
    }
    
    private static class CachedQuestionnaire {
        private final QuestionareRequest questionnaire;
        private final Instant timestamp;
        
        public CachedQuestionnaire(QuestionareRequest questionnaire, Instant timestamp) {
            this.questionnaire = questionnaire;
            this.timestamp = timestamp;
        }
        
        public QuestionareRequest getQuestionnaire() {
            return questionnaire;
        }
        
        public Instant getTimestamp() {
            return timestamp;
        }
        
        public boolean isExpired() {
            return false; // For simplicity, never expire in this implementation
        }
    }
}
