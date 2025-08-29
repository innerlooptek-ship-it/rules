package com.cvshealth.digital.microservice.iqe.service.strategy;

import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
import com.cvshealth.digital.microservice.iqe.service.EnhancedRedisCacheService;
import com.cvshealth.digital.microservice.iqe.service.CassandraService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.cvshealth.digital.microservice.iqe.service.CacheMetricsService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class CassandraFirstStrategy implements CachingStrategy {
    
    private final EnhancedRedisCacheService redisCacheService;
    private final CassandraService cassandraService;
    private final CacheMetricsService metricsService;
    
    @Override
    public Mono<QuestionareRequest> getQuestionnaire(String actionId) {
        log.debug("Cassandra-first strategy: Querying Cassandra first for actionId: {}", actionId);
        
        return cassandraService.getQuestionnaire(actionId)
            .doOnNext(result -> {
                log.info("Cassandra query SUCCESS for actionId: {}", actionId);
                metricsService.recordCacheHit("cassandra");
            })
            .switchIfEmpty(
                Mono.defer(() -> {
                    log.info("Cassandra query returned empty for actionId: {}, trying Redis fallback", actionId);
                    return redisCacheService.getFromTableCache(actionId)
                        .doOnNext(cached -> log.info("Redis fallback SUCCESS for actionId: {}", actionId))
                        .doOnError(error -> log.error("Redis fallback FAILED for actionId: {}", actionId, error));
                })
            )
            .onErrorResume(error -> {
                log.warn("Cassandra query FAILED for actionId: {}, trying Redis fallback", actionId, error);
                return redisCacheService.getFromTableCache(actionId)
                    .doOnNext(cached -> log.info("Redis fallback SUCCESS after Cassandra error for actionId: {}", actionId))
                    .doOnError(fallbackError -> log.error("Both Cassandra and Redis failed for actionId: {}", actionId, fallbackError));
            });
    }
    
    @Override
    public Mono<Void> invalidateCache(String actionId) {
        log.debug("Invalidating Redis cache for actionId: {}", actionId);
        return redisCacheService.invalidateTableCache("questions", actionId)
            .then(redisCacheService.invalidateTableCache("answer_options", actionId))
            .then(redisCacheService.invalidateTableCache("actions", actionId))
            .then(redisCacheService.invalidateTableCache("questions_details", actionId));
    }
    
    @Override
    public Mono<Void> refreshCache(String actionId) {
        log.debug("Refreshing cache via Cassandra-first strategy for actionId: {}", actionId);
        return cassandraService.getQuestionnaire(actionId)
            .flatMap(redisCacheService::cacheQuestionnaire)
            .doOnSuccess(v -> log.debug("Cache refresh completed for actionId: {}", actionId))
            .onErrorResume(error -> {
                log.warn("Cache refresh failed for actionId: {}", actionId, error);
                return Mono.empty();
            });
    }
}
