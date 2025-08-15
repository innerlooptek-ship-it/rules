package com.cvshealth.digital.microservice.iqe.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class CassandraHealthIndicator {
    
    private final CassandraTemplate cassandraTemplate;
    private volatile boolean isHealthy = true;
    private volatile Instant lastHealthCheck = Instant.now();
    private static final Duration HEALTH_CHECK_CACHE_DURATION = Duration.ofSeconds(30);
    
    public boolean checkHealth() {
        try {
            cassandraTemplate.getCqlOperations().execute("SELECT now() FROM system.local");
            isHealthy = true;
            lastHealthCheck = Instant.now();
            log.debug("Cassandra health check passed");
            return true;
        } catch (Exception e) {
            isHealthy = false;
            lastHealthCheck = Instant.now();
            log.error("Cassandra health check failed", e);
            return false;
        }
    }
    
    public Mono<Boolean> isHealthy() {
        if (Duration.between(lastHealthCheck, Instant.now()).compareTo(HEALTH_CHECK_CACHE_DURATION) > 0) {
            return Mono.fromCallable(this::checkHealth)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(healthy -> log.debug("Cassandra health check result: {}", healthy))
                .onErrorReturn(false);
        }
        return Mono.just(isHealthy);
    }
    
    public void forceHealthCheck() {
        lastHealthCheck = Instant.EPOCH;
    }
}
