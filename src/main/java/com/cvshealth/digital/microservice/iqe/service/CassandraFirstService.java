package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.config.EnhancedRedisConfig;
import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class CassandraFirstService {
    
    private final CassandraService cassandraService;
    private final DatasetFirstReadService datasetFirstReadService;
    private final EnhancedRedisConfig config;
    
    public Mono<QuestionareRequest> getQuestionnaireWithFallback(String actionId) {
        log.debug("Cassandra-first read for actionId: {}", actionId);
        
        return cassandraService.getQuestionnaire(actionId)
            .doOnNext(result -> log.debug("Cassandra read SUCCESS for actionId: {}", actionId))
            .switchIfEmpty(
                Mono.defer(() -> {
                    log.warn("Cassandra read FAILED for actionId: {}, falling back to Redis dataset", actionId);
                    return datasetFirstReadService.getQuestionnaireFromDataset(actionId)
                        .doOnNext(result -> log.info("Redis dataset fallback SUCCESS for actionId: {}", actionId));
                })
            )
            .onErrorResume(error -> {
                log.warn("Cassandra error for actionId: {}, falling back to Redis dataset", actionId, error);
                return datasetFirstReadService.getQuestionnaireFromDataset(actionId)
                    .doOnNext(result -> log.info("Redis dataset fallback SUCCESS after Cassandra error for actionId: {}", actionId))
                    .doOnError(fallbackError -> log.error("Both Cassandra and Redis dataset failed for actionId: {}", actionId, fallbackError));
            });
    }
}
