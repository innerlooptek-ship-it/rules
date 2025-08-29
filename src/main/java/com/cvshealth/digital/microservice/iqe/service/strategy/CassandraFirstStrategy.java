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
        return cassandraService.getQuestionnaire(actionId)
            .doOnNext(result -> {
                log.debug("Loaded from Cassandra for actionId: {}", actionId);
                metricsService.recordCacheHit("cassandra");
            })
            .flatMap(result -> redisCacheService.cacheQuestionnaire(result)
                .thenReturn(result));
    }
    
    @Override
    public Mono<Void> invalidateCache(String actionId) {
        return redisCacheService.invalidateTableCache("questions", actionId)
            .then(redisCacheService.invalidateTableCache("answer_options", actionId))
            .then(redisCacheService.invalidateTableCache("actions", actionId));
    }
    
    @Override
    public Mono<Void> refreshCache(String actionId) {
        return getQuestionnaire(actionId).then();
    }
}
