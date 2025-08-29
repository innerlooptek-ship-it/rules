package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.config.EnhancedRedisConfig;
import com.cvshealth.digital.microservice.iqe.repository.ActionsRepository;
import com.cvshealth.digital.microservice.iqe.utils.FeatureProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheWarmupService {
    
    private final ActionsRepository actionsRepository;
    private final CassandraService cassandraService;
    private final EnhancedRedisCacheService redisCacheService;
    private final EnhancedRedisConfig config;
    private final FeatureProperties featureProperties;
    
    @EventListener(ApplicationReadyEvent.class)
    public void warmupCacheOnStartup() {
        if (!featureProperties.isEnhancedRedisCachingEnabled()) {
            log.info("Enhanced Redis caching disabled, skipping cache warmup");
            return;
        }
        
        log.info("Starting initial cache warmup for all questionnaire data");
        performBulkCacheWarmup()
            .subscribe(
                count -> log.info("Initial cache warmup completed: {} questionnaires cached", count),
                error -> log.error("Initial cache warmup failed", error)
            );
    }
    
    @Scheduled(fixedRate = 14400000) // Every 4 hours
    public void scheduledCacheRefresh() {
        if (!featureProperties.isEnhancedRedisCachingEnabled()) {
            return;
        }
        
        log.info("Starting scheduled cache refresh");
        performBulkCacheWarmup()
            .subscribe(
                count -> log.info("Scheduled cache refresh completed: {} questionnaires refreshed", count),
                error -> log.error("Scheduled cache refresh failed", error)
            );
    }
    
    private Mono<Integer> performBulkCacheWarmup() {
        AtomicInteger cachedCount = new AtomicInteger(0);
        
        return actionsRepository.findAll()
            .flatMap(action -> {
                String actionId = action.getActionId();
                return cassandraService.getQuestionnaire(actionId)
                    .flatMap(questionnaire -> {
                        if (questionnaire.getActions() != null && questionnaire.getActions().getActionId() != null) {
                            return redisCacheService.cacheQuestionnaire(questionnaire)
                                .doOnSuccess(v -> {
                                    cachedCount.incrementAndGet();
                                    log.debug("Cached questionnaire for actionId: {}", actionId);
                                })
                                .onErrorResume(e -> {
                                    log.warn("Failed to cache questionnaire for actionId: {}", actionId, e);
                                    return Mono.empty();
                                });
                        }
                        return Mono.empty();
                    })
                    .onErrorResume(e -> {
                        log.warn("Failed to load questionnaire from Cassandra for actionId: {}", actionId, e);
                        return Mono.empty();
                    });
            })
            .then(Mono.fromCallable(cachedCount::get))
            .timeout(Duration.ofMinutes(30))
            .doOnSubscribe(s -> log.info("Starting bulk cache warmup process"))
            .doOnSuccess(count -> log.info("Bulk cache warmup process completed successfully"));
    }
    
    public Mono<Void> warmupSpecificActionIds(String... actionIds) {
        log.info("Warming up cache for specific actionIds: {}", String.join(", ", actionIds));
        
        return Flux.fromArray(actionIds)
            .flatMap(actionId -> 
                cassandraService.getQuestionnaire(actionId)
                    .flatMap(redisCacheService::cacheQuestionnaire)
                    .doOnSuccess(v -> log.debug("Warmed up cache for actionId: {}", actionId))
                    .onErrorResume(e -> {
                        log.warn("Failed to warm up cache for actionId: {}", actionId, e);
                        return Mono.empty();
                    })
            )
            .then()
            .doOnSuccess(v -> log.info("Specific actionIds cache warmup completed"));
    }
}
