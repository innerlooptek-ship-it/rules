package com.cvshealth.digital.microservice.iqe.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "api")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiConfigs {

    private Map<String, Map<String,ApiConfig>> configs;

    @Data
    public static class ApiConfig {
        private String baseUrl;
        private String uri;
        private int readTimeout;
        private int connectionTimeout;
        private String userName;
        private String password;
        private String expiryTime;
        private String auth;
        private String clientId;
        private String clientSecret;
        private String encryptionKey;

    }
}