package com.cvshealth.digital.microservice.iqe.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatasetCacheWarmupService {
    
    private final SimplifiedRedisDataService redisDataService;
    
    @EventListener(ApplicationReadyEvent.class)
    public void warmupDatasetCache() {
        log.info("Starting dataset cache warmup with sample data...");
        
        try {
            redisDataService.loadSampleTestData();
            log.info("Dataset cache warmup completed successfully with sample data");
        } catch (Exception e) {
            log.warn("Dataset cache warmup failed: {}", e.getMessage());
        }
    }
}
