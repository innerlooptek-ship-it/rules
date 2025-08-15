package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
import com.cvshealth.digital.microservice.iqe.health.CassandraHealthIndicator;
import com.cvshealth.digital.microservice.iqe.model.WriteOperation;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

import static com.cvshealth.digital.microservice.iqe.constants.SchedulingConstants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheFallbackService {
    
    private final RedisCacheService redisCacheService;
    private final CassandraHealthIndicator cassandraHealthIndicator;
    private final ObjectMapper objectMapper;
    private final ConcurrentLinkedQueue<WriteOperation> pendingWrites = new ConcurrentLinkedQueue<>();
    
    public Mono<QuestionareRequest> getQuestionnaireWithFallback(String actionId, 
                                                                QuestionareRequest iqeOutput,
                                                                Supplier<Mono<QuestionareRequest>> cassandraFallback) {
        return cassandraHealthIndicator.isHealthy()
            .flatMap(isHealthy -> {
                if (isHealthy) {
                    return cassandraFallback.get()
                        .doOnSuccess(result -> cacheResult(actionId, result));
                } else {
                    log.warn("Cassandra is unhealthy, falling back to Redis cache for actionId: {}", actionId);
                    return getFromRedisCache(actionId, iqeOutput)
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
    
    private Mono<QuestionareRequest> getFromRedisCache(String actionId, QuestionareRequest iqeOutput) {
        Map<String, String> eventMap = Map.of("operation", "fallback_read", "actionId", actionId);
        
        return redisCacheService.getDataFromRedis(IQE_QUESTIONNAIRE, actionId, eventMap)
            .map(jsonNode -> {
                try {
                    return objectMapper.convertValue(jsonNode, QuestionareRequest.class);
                } catch (Exception e) {
                    log.error("Failed to deserialize cached questionnaire for actionId: {}", actionId, e);
                    return null;
                }
            })
            .doOnSuccess(result -> {
                if (result != null) {
                    log.info("Successfully retrieved questionnaire from Redis fallback cache: {}", actionId);
                } else {
                    log.warn("No cached questionnaire found in Redis for actionId: {}", actionId);
                }
            });
    }
    
    private void cacheResult(String actionId, QuestionareRequest questionnaire) {
        if (questionnaire != null) {
            Map<String, String> eventMap = Map.of("operation", "cache_write", "actionId", actionId);
            redisCacheService.setDataToRedisRest(actionId, questionnaire, eventMap)
                .doOnSuccess(result -> log.debug("Cached questionnaire result for actionId: {}", actionId))
                .doOnError(e -> log.error("Failed to cache questionnaire for actionId: {}", actionId, e))
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
    
    private QuestionareRequest createUnavailableResponse(QuestionareRequest iqeOutput) {
        iqeOutput.setStatusCode(SERVICE_UNAVAILABLE_CODE);
        iqeOutput.setErrorDescription("Service temporarily unavailable. Please try again later.");
        log.warn("Created unavailable response due to both Cassandra and Redis being inaccessible");
        return iqeOutput;
    }
}
