package com.cvshealth.digital.microservice.iqe.health;

import com.cvshealth.digital.microservice.iqe.config.EnhancedRedisConfig;
import com.cvshealth.digital.microservice.iqe.service.EnhancedRedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheHealthIndicator implements HealthIndicator {
    
    private final ReactiveCassandraTemplate cassandraTemplate;
    private final EnhancedRedisConfig config;
    private final EnhancedRedisCacheService redisCacheService;
    
    @Override
    public Health health() {
        try {
            cassandraTemplate.select("SELECT now() FROM system.local", Object.class)
                .blockFirst(Duration.ofSeconds(2));
            
            boolean redisHealthy = true;
            try {
                redisCacheService.getFromCache("health-check").block(Duration.ofSeconds(1));
            } catch (Exception e) {
                redisHealthy = false;
                log.warn("Redis health check failed: {}", e.getMessage());
            }
                
            return Health.up()
                .withDetail("redis", redisHealthy ? "UP" : "DOWN")
                .withDetail("cassandra", "UP")
                .withDetail("strategy", config.getStrategy())
                .withDetail("table-caching", config.getTableCaching().isEnabled())
                .withDetail("enhanced-caching", config.isEnabled())
                .build();
                
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .withDetail("cassandra", "DOWN")
                .build();
        }
    }
}
