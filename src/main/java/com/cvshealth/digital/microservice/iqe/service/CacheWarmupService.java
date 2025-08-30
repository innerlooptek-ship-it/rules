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
    private final DatasetSnapshotService datasetSnapshotService;
    private final EnhancedRedisCacheService redisCacheService;
    private final EnhancedRedisConfig config;
    private final FeatureProperties featureProperties;
    
    @EventListener(ApplicationReadyEvent.class)
    public void warmupDatasetOnStartup() {
        if (!featureProperties.isDatasetSnapshotEnabled()) {
            log.info("Dataset snapshot caching disabled, skipping dataset warmup");
            return;
        }
        
        log.info("Starting initial dataset warmup (Strategy 1: Dataset Snapshot)");
        datasetSnapshotService.createCompleteDatasetSnapshot()
            .subscribe(
                v -> log.info("Initial dataset warmup completed successfully"),
                error -> log.error("Initial dataset warmup failed", error)
            );
    }
    
    @Scheduled(fixedRateString = "#{${service.redis-cache.dataset-refresh.interval-hours:4} * 3600000}")
    public void scheduledDatasetRefresh() {
        if (!featureProperties.isDatasetSnapshotEnabled() || 
            !config.getDatasetRefresh().isEnabled()) {
            return;
        }
        
        log.info("Starting scheduled dataset refresh (Strategy 1: Dataset Snapshot)");
        datasetSnapshotService.createCompleteDatasetSnapshot()
            .subscribe(
                v -> log.info("Scheduled dataset refresh completed successfully"),
                error -> log.error("Scheduled dataset refresh failed", error)
            );
    }
    
}
