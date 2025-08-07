package com.cvshealth.digital.microservice.iqe.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * REDIS client configuration.
 *
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisConfigProperties redisConfigProperties() {
        return new RedisConfigProperties();
    }

    @ConfigurationProperties(prefix = "service.redis-cache")
    @Data
    public static class RedisConfigProperties {

        private String baseUrl;
        private Integer readTimeOut;
        private Integer writeTimeOut;
        private Integer connectionTimeOut;
        private String timeoutErrorMessage;
        private String cacheType;

    }
}