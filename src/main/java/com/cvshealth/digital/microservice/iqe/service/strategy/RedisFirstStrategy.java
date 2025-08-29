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
public class RedisFirstStrategy implements CachingStrategy {
    
    private final EnhancedRedisCacheService redisCacheService;
    private final CassandraService cassandraService;
    private final CacheMetricsService metricsService;
    
    @Override
    public Mono<QuestionareRequest> getQuestionnaire(String actionId) {
        log.debug("Redis-first strategy: Querying Redis first for actionId: {}", actionId);
        
        return redisCacheService.getFromTableCache(actionId)
            .doOnNext(cached -> {
                log.info("Redis cache HIT for actionId: {}", actionId);
                metricsService.recordCacheHit("redis");
            })
            .switchIfEmpty(
                Mono.defer(() -> {
                    log.info("Redis cache MISS for actionId: {}, falling back to Cassandra", actionId);
                    metricsService.recordCacheMiss("redis");
                    return cassandraService.getQuestionnaire(actionId)
                        .doOnNext(result -> log.info("Cassandra fallback SUCCESS for actionId: {}", actionId))
                        .doOnError(error -> log.error("Cassandra fallback FAILED for actionId: {}", actionId, error))
                        .onErrorResume(error -> {
                            log.warn("Both Redis and Cassandra unavailable for actionId: {}", actionId);
                            return Mono.empty();
                        });
                })
            );
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
        log.debug("Refreshing Redis cache for actionId: {}", actionId);
        return cassandraService.getQuestionnaire(actionId)
            .flatMap(redisCacheService::cacheQuestionnaire)
            .doOnSuccess(v -> log.debug("Cache refresh completed for actionId: {}", actionId))
            .onErrorResume(error -> {
                log.warn("Cache refresh failed for actionId: {}", actionId, error);
                return Mono.empty();
            });
    }
}
