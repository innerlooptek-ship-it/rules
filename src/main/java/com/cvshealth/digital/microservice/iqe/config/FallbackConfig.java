package com.cvshealth.digital.microservice.iqe.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FallbackConfig {

    @Bean
    public RedisFallbackProperties redisFallbackProperties() {
        return new RedisFallbackProperties();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @ConfigurationProperties(prefix = "service.redis-fallback")
    @Data
    public static class RedisFallbackProperties {
        private boolean enabled = false;
        private int cacheTtlHours = 24;
        private int cacheRefreshIntervalMinutes = 60;
        private int healthCheckIntervalSeconds = 30;
        private int writeQueueMaxSize = 1000;
        private boolean cacheWarmingEnabled = true;
        private String cacheKeyPrefix = "fallback:";
    }
}
