package com.cvshealth.digital.microservice.iqe.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "service.static-json-fallback")
@Data
public class StaticJsonConfig {
    
    private boolean enabled = false;
    private String dataDirectory = "/tmp/iqe-fallback";
    private boolean memoryCache = true;
    private int maxCacheSize = 1000;
    private long cacheExpirationMinutes = 60;
    private boolean autoCreateDirectory = true;
    private String fileExtension = ".json";
    private boolean compressFiles = false;
}
