package com.cvshealth.digital.microservice.iqe.service.strategy;

import com.cvshealth.digital.microservice.iqe.config.EnhancedRedisConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CachingStrategyFactory {
    
    private final RedisFirstStrategy redisFirstStrategy;
    private final CassandraFirstStrategy cassandraFirstStrategy;
    
    public CachingStrategy getStrategy(EnhancedRedisConfig.CachingStrategy strategy) {
        return switch (strategy) {
            case REDIS_FIRST -> redisFirstStrategy;
            case CASSANDRA_FIRST -> cassandraFirstStrategy;
        };
    }
}
