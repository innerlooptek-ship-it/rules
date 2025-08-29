package com.cvshealth.digital.microservice.iqe.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "service.redis-cache")
@Data
public class EnhancedRedisConfig {
    
    private boolean enabled = true;
    private String strategy = "redis-first";
    private boolean redisFlag = true;
    private String baseUrl;
    private Integer readTimeOut;
    private Integer writeTimeOut;
    private Integer connectionTimeOut;
    private String cacheType;
    
    private TableCaching tableCaching = new TableCaching();
    private Synchronization synchronization = new Synchronization();
    private Fallback fallback = new Fallback();
    
    @Data
    public static class TableCaching {
        private boolean enabled = true;
        private String strategy = "selective";
        private Map<String, TableConfig> tables;
    }
    
    @Data
    public static class TableConfig {
        private boolean enabled = true;
        private String ttl = "2h";
        private String keyPattern;
    }
    
    @Data
    public static class Synchronization {
        private boolean enabled = true;
        private String invalidationStrategy = "immediate";
        private boolean writeThrough = false;
        private boolean writeBehind = true;
    }
    
    @Data
    public static class Fallback {
        private String cassandraTimeout = "5s";
        private String redisTimeout = "2s";
        private CircuitBreaker circuitBreaker = new CircuitBreaker();
    }
    
    @Data
    public static class CircuitBreaker {
        private boolean enabled = true;
        private int failureThreshold = 5;
        private String recoveryTimeout = "30s";
    }
    
    public enum CachingStrategy {
        REDIS_FIRST, CASSANDRA_FIRST
    }
    
    public CachingStrategy getStrategyEnum() {
        return CachingStrategy.valueOf(strategy.toUpperCase().replace("-", "_"));
    }
}
