package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.config.EnhancedRedisConfig;
import com.cvshealth.digital.microservice.iqe.repository.ActionsRepository;
import com.cvshealth.digital.microservice.iqe.service.strategy.CachingStrategyFactory;
import com.cvshealth.digital.microservice.iqe.utils.FeatureProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheWarmupService {
    
    private final ActionsRepository actionsRepository;
    private final CachingStrategyFactory strategyFactory;
    private final EnhancedRedisConfig config;
    private final FeatureProperties featureProperties;
    
    @EventListener(ApplicationReadyEvent.class)
    public void warmupCache() {
        if (!featureProperties.isEnhancedRedisCachingEnabled()) {
            log.info("Enhanced Redis caching disabled, skipping cache warmup");
            return;
        }
        
        log.info("Starting cache warmup for frequently accessed questionnaires");
        
        actionsRepository.findAll()
            .take(10)
            .flatMap(action -> {
                String actionId = action.getActionId();
                return strategyFactory.getStrategy(config.getStrategyEnum())
                    .getQuestionnaire(actionId)
                    .doOnNext(result -> log.debug("Warmed up cache for actionId: {}", actionId))
                    .onErrorResume(e -> {
                        log.warn("Failed to warm up cache for actionId: {}", actionId, e);
                        return Mono.empty();
                    });
            })
            .subscribe(
                result -> {},
                error -> log.error("Cache warmup failed", error),
                () -> log.info("Cache warmup completed")
            );
    }
}
