package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.config.EnhancedRedisConfig;
import com.cvshealth.digital.microservice.iqe.service.strategy.CachingStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheSynchronizationService {
    
    private final EnhancedRedisCacheService redisCacheService;
    private final CachingStrategyFactory strategyFactory;
    private final EnhancedRedisConfig config;
    
    @EventListener
    public void handleCassandraUpdate(CassandraUpdateEvent event) {
        if (!config.getSynchronization().isEnabled()) {
            return;
        }
        
        switch (event.getTable()) {
            case "actions":
                invalidateActionCache(event.getActionId()).subscribe();
                break;
            case "questions":
                invalidateQuestionCache(event.getActionId()).subscribe();
                break;
            case "rules_by_flow":
                invalidateRulesCache(event.getFlow()).subscribe();
                break;
            case "answer_options":
                invalidateAnswerOptionsCache(event.getActionId()).subscribe();
                break;
            case "questions_details":
                invalidateDetailsCache(event.getActionId()).subscribe();
                break;
        }
    }
    
    private Mono<Void> invalidateActionCache(String actionId) {
        return redisCacheService.invalidateTableCache("actions", actionId)
            .doOnSuccess(v -> log.debug("Invalidated action cache for actionId: {}", actionId))
            .then(refreshCacheIfConfigured(actionId));
    }
    
    private Mono<Void> invalidateQuestionCache(String actionId) {
        return redisCacheService.invalidateTableCache("questions", actionId)
            .doOnSuccess(v -> log.debug("Invalidated question cache for actionId: {}", actionId))
            .then(refreshCacheIfConfigured(actionId));
    }
    
    private Mono<Void> invalidateRulesCache(String flow) {
        return redisCacheService.invalidateTableCache("rules_by_flow", flow)
            .doOnSuccess(v -> log.debug("Invalidated rules cache for flow: {}", flow));
    }
    
    private Mono<Void> invalidateAnswerOptionsCache(String actionId) {
        return redisCacheService.invalidateTableCache("answer_options", actionId)
            .doOnSuccess(v -> log.debug("Invalidated answer options cache for actionId: {}", actionId));
    }
    
    private Mono<Void> invalidateDetailsCache(String actionId) {
        return redisCacheService.invalidateTableCache("questions_details", actionId)
            .doOnSuccess(v -> log.debug("Invalidated details cache for actionId: {}", actionId));
    }
    
    private Mono<Void> refreshCacheIfConfigured(String actionId) {
        if ("immediate".equals(config.getSynchronization().getInvalidationStrategy())) {
            return strategyFactory.getStrategy(config.getStrategyEnum())
                .refreshCache(actionId);
        }
        return Mono.empty();
    }
    
    public static class CassandraUpdateEvent {
        private final String table;
        private final String actionId;
        private final String flow;
        
        public CassandraUpdateEvent(String table, String actionId, String flow) {
            this.table = table;
            this.actionId = actionId;
            this.flow = flow;
        }
        
        public String getTable() { return table; }
        public String getActionId() { return actionId; }
        public String getFlow() { return flow; }
    }
}
