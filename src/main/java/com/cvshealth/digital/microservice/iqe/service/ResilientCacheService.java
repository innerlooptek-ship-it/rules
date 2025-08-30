package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.config.EnhancedRedisConfig;
import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
import com.cvshealth.digital.microservice.iqe.error.ServiceUnavailableException;
import com.cvshealth.digital.microservice.iqe.service.strategy.CachingStrategy;
import com.cvshealth.digital.microservice.iqe.service.strategy.CachingStrategyFactory;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResilientCacheService {
    
    private final CachingStrategyFactory strategyFactory;
    private final EnhancedRedisConfig config;
    private final EnhancedRedisCacheService redisCacheService;
    
    private CircuitBreaker cassandraCircuitBreaker;
    private CircuitBreaker redisCircuitBreaker;
    
    public Mono<QuestionareRequest> getQuestionnaireWithResilience(String actionId) {
        if (!config.getFallback().getCircuitBreaker().isEnabled()) {
            return normalFlow(actionId);
        }
        
        initializeCircuitBreakers();
        
        return Mono.defer(() -> {
            if (cassandraCircuitBreaker.getState() == CircuitBreaker.State.OPEN) {
                log.warn("Cassandra circuit breaker is OPEN, using Redis-only fallback");
                return redisOnlyFallback(actionId);
            }
            
            return normalFlow(actionId)
                .onErrorResume(throwable -> {
                    if (isTimeoutException(throwable)) {
                        log.warn("Cassandra timeout detected, falling back to Redis");
                        return redisOnlyFallback(actionId);
                    }
                    return Mono.error(throwable);
                });
        });
    }
    
    private Mono<QuestionareRequest> normalFlow(String actionId) {
        CachingStrategy strategy = strategyFactory.getStrategy(config.getStrategyEnum());
        return strategy.getQuestionnaire(actionId);
    }
    
    private Mono<QuestionareRequest> redisOnlyFallback(String actionId) {
        return redisCacheService.getFromTableCache(actionId)
            .switchIfEmpty(Mono.error(new ServiceUnavailableException(
                "Both Cassandra and Redis dataset unavailable for actionId: " + actionId)));
    }
    
    private void initializeCircuitBreakers() {
        if (cassandraCircuitBreaker == null) {
            CircuitBreakerConfig cassandraConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.parse("PT" + config.getFallback().getCircuitBreaker().getRecoveryTimeout()))
                .slidingWindowSize(config.getFallback().getCircuitBreaker().getFailureThreshold())
                .build();
            
            cassandraCircuitBreaker = CircuitBreaker.of("cassandra", cassandraConfig);
        }
        
        if (redisCircuitBreaker == null) {
            CircuitBreakerConfig redisConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.parse("PT" + config.getFallback().getCircuitBreaker().getRecoveryTimeout()))
                .slidingWindowSize(config.getFallback().getCircuitBreaker().getFailureThreshold())
                .build();
            
            redisCircuitBreaker = CircuitBreaker.of("redis", redisConfig);
        }
    }
    
    private boolean isTimeoutException(Throwable throwable) {
        return throwable instanceof TimeoutException || 
               throwable.getCause() instanceof TimeoutException ||
               throwable.getMessage().toLowerCase().contains("timeout");
    }
}
