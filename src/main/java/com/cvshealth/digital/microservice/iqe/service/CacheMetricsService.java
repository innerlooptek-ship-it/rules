package com.cvshealth.digital.microservice.iqe.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CacheMetricsService {
    
    private final MeterRegistry meterRegistry;
    
    private Counter cacheHitCounter;
    private Counter cacheMissCounter;
    private Timer cacheOperationTimer;
    
    public void recordCacheHit(String cacheType) {
        if (cacheHitCounter == null) {
            cacheHitCounter = Counter.builder("cache.hit")
                .tag("type", cacheType)
                .register(meterRegistry);
        }
        cacheHitCounter.increment();
    }
    
    public void recordCacheMiss(String cacheType) {
        if (cacheMissCounter == null) {
            cacheMissCounter = Counter.builder("cache.miss")
                .tag("type", cacheType)
                .register(meterRegistry);
        }
        cacheMissCounter.increment();
    }
    
    public Timer.Sample startTimer() {
        if (cacheOperationTimer == null) {
            cacheOperationTimer = Timer.builder("cache.operation.duration")
                .register(meterRegistry);
        }
        return Timer.start(meterRegistry);
    }
}
