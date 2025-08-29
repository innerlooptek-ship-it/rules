package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.config.EnhancedRedisConfig;
import com.cvshealth.digital.microservice.iqe.utils.FeatureProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheConfigurationService {
    
    private final EnhancedRedisConfig redisConfig;
    private final FeatureProperties featureProperties;
    
    public boolean isEnhancedCachingEnabled() {
        return featureProperties.isEnhancedRedisCachingEnabled() && redisConfig.isEnabled();
    }
    
    public boolean isTableLevelCachingEnabled() {
        return isEnhancedCachingEnabled() && 
               featureProperties.isTableLevelCachingEnabled() && 
               redisConfig.getTableCaching().isEnabled();
    }
    
    public boolean isRedisFirstStrategy() {
        return EnhancedRedisConfig.CachingStrategy.REDIS_FIRST.equals(redisConfig.getStrategyEnum());
    }
    
    public boolean isCassandraFirstStrategy() {
        return EnhancedRedisConfig.CachingStrategy.CASSANDRA_FIRST.equals(redisConfig.getStrategyEnum());
    }
    
    public boolean isSynchronizationEnabled() {
        return isEnhancedCachingEnabled() && redisConfig.getSynchronization().isEnabled();
    }
    
    public boolean isCircuitBreakerEnabled() {
        return isEnhancedCachingEnabled() && redisConfig.getFallback().getCircuitBreaker().isEnabled();
    }
    
    public void logCurrentConfiguration() {
        log.info("Enhanced Redis Caching Configuration:");
        log.info("  Enhanced Caching Enabled: {}", isEnhancedCachingEnabled());
        log.info("  Table Level Caching Enabled: {}", isTableLevelCachingEnabled());
        log.info("  Strategy: {}", redisConfig.getStrategy());
        log.info("  Synchronization Enabled: {}", isSynchronizationEnabled());
        log.info("  Circuit Breaker Enabled: {}", isCircuitBreakerEnabled());
    }
}
