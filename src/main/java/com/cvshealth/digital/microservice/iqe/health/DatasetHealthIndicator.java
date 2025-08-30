package com.cvshealth.digital.microservice.iqe.health;

import com.cvshealth.digital.microservice.iqe.config.EnhancedRedisConfig;
import com.cvshealth.digital.microservice.iqe.service.DatasetSnapshotService;
import com.cvshealth.digital.microservice.iqe.service.EnhancedRedisCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class DatasetHealthIndicator implements HealthIndicator {
    
    private final EnhancedRedisCacheService redisCacheService;
    private final EnhancedRedisConfig config;
    
    @Override
    public Health health() {
        try {
            DatasetSnapshotService.DatasetMetadata metadata = (DatasetSnapshotService.DatasetMetadata) 
                redisCacheService.getFromCache(config.getDatasetKeys().getMetadataKey())
                    .cast(DatasetSnapshotService.DatasetMetadata.class)
                    .block(Duration.ofSeconds(2));
            
            if (metadata != null && metadata.getLastRefresh() != null) {
                Instant refreshThreshold = Instant.now().minus(config.getDatasetRefresh().getIntervalHours() + 1, ChronoUnit.HOURS);
                
                if (metadata.getLastRefresh().isAfter(refreshThreshold)) {
                    return Health.up()
                        .withDetail("strategy", "dataset-snapshot")
                        .withDetail("cassandra-first", config.getReadStrategy().isCassandraFirst())
                        .withDetail("redis-fallback-only", config.getReadStrategy().isRedisFallbackOnly())
                        .withDetail("last-refresh", metadata.getLastRefresh())
                        .withDetail("dataset-version", metadata.getVersion())
                        .withDetail("total-rules", metadata.getTotalRules())
                        .withDetail("total-actions", metadata.getTotalActions())
                        .withDetail("total-questions", metadata.getTotalQuestions())
                        .build();
                } else {
                    return Health.down()
                        .withDetail("issue", "Dataset refresh overdue")
                        .withDetail("last-refresh", metadata.getLastRefresh())
                        .build();
                }
            } else {
                return Health.down()
                    .withDetail("issue", "Dataset metadata not found")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
