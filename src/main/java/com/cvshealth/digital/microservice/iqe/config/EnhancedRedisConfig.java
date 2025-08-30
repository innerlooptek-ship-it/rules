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
    
    private DatasetRefresh datasetRefresh = new DatasetRefresh();
    private ResponseCaching responseCaching = new ResponseCaching();
    private DatasetKeys datasetKeys = new DatasetKeys();
    private ReadStrategy readStrategy = new ReadStrategy();
    private Fallback fallback = new Fallback();
    
    @Data
    public static class DatasetRefresh {
        private boolean enabled = true;
        private int intervalHours = 4;
        private int batchSize = 100;
        private int timeoutMinutes = 30;
        private int retryAttempts = 3;
    }
    
    @Data
    public static class ResponseCaching {
        private boolean enabled = false;  // Disabled for Strategy 1
    }
    
    @Data
    public static class DatasetKeys {
        private String rulesPrefix = "dataset:rules_by_flow:";
        private String actionsKey = "dataset:actions";
        private String questionsPrefix = "dataset:questions:";
        private String answerOptionsPrefix = "dataset:answer_options:";
        private String detailsPrefix = "dataset:questions_details:";
        private String metadataKey = "dataset:metadata";
    }
    
    @Data
    public static class ReadStrategy {
        private boolean cassandraFirst = true;
        private boolean redisFallbackOnly = true;
        private int timeoutMs = 5000;
        private int retryAttempts = 2;
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
        REDIS_FIRST, CASSANDRA_FIRST, DATASET_SNAPSHOT
    }
    
    public CachingStrategy getStrategyEnum() {
        return CachingStrategy.valueOf(strategy.toUpperCase().replace("-", "_"));
    }
}
