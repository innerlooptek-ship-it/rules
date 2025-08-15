package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.config.GcsConfig;
import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
import com.cvshealth.digital.microservice.iqe.health.CassandraHealthIndicator;
import com.cvshealth.digital.microservice.iqe.model.WriteOperation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

import static com.cvshealth.digital.microservice.iqe.constants.SchedulingConstants.*;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "service.gcs-fallback.enabled", havingValue = "true")
public class GcsFallbackService {
    
    private final Storage storage;
    private final GcsConfig gcsConfig;
    private final ObjectMapper objectMapper;
    private final CassandraHealthIndicator cassandraHealthIndicator;
    private final ConcurrentLinkedQueue<WriteOperation> pendingWrites = new ConcurrentLinkedQueue<>();
    
    public Mono<QuestionareRequest> getQuestionnaireWithFallback(String actionId, 
                                                                QuestionareRequest iqeOutput,
                                                                Supplier<Mono<QuestionareRequest>> cassandraFallback) {
        return cassandraHealthIndicator.isHealthy()
            .flatMap(isHealthy -> {
                if (isHealthy) {
                    return cassandraFallback.get()
                        .doOnSuccess(result -> uploadToGcs(actionId, result));
                } else {
                    log.warn("Cassandra is unhealthy, falling back to GCS for actionId: {}", actionId);
                    return getFromGcs(actionId, iqeOutput)
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
    
    private Mono<QuestionareRequest> getFromGcs(String actionId, QuestionareRequest iqeOutput) {
        return Mono.fromCallable(() -> {
            String blobName = gcsConfig.getKeyPrefix() + actionId + ".json";
            Blob blob = storage.get(gcsConfig.getBucketName(), blobName);
            
            if (blob != null && blob.exists()) {
                String content = new String(blob.getContent());
                return objectMapper.readValue(content, QuestionareRequest.class);
            }
            return null;
        })
        .subscribeOn(Schedulers.boundedElastic())
        .doOnSuccess(result -> {
            if (result != null) {
                log.info("Successfully retrieved questionnaire from GCS fallback: {}", actionId);
            } else {
                log.warn("No questionnaire found in GCS for actionId: {}", actionId);
            }
        })
        .doOnError(e -> log.error("Failed to retrieve from GCS for actionId: {}", actionId, e));
    }
    
    private void uploadToGcs(String actionId, QuestionareRequest questionnaire) {
        if (questionnaire != null) {
            Mono.fromCallable(() -> {
                String blobName = gcsConfig.getKeyPrefix() + actionId + ".json";
                String content = objectMapper.writeValueAsString(questionnaire);
                
                BlobInfo blobInfo = BlobInfo.newBuilder(gcsConfig.getBucketName(), blobName)
                    .setContentType("application/json")
                    .build();
                    
                storage.create(blobInfo, content.getBytes());
                return null;
            })
            .subscribeOn(Schedulers.boundedElastic())
            .doOnSuccess(result -> log.debug("Uploaded questionnaire to GCS: {}", actionId))
            .doOnError(e -> log.error("Failed to upload to GCS for actionId: {}", actionId, e))
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
        log.warn("Created unavailable response due to both Cassandra and GCS being inaccessible");
        return iqeOutput;
    }
}
