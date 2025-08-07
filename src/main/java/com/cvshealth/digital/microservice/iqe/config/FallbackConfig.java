package com.cvshealth.digital.microservice.iqe.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "service.fallback-cache")
public class FallbackConfig {

    private boolean enabled = true;
    private int cacheTtlHours = 24;
    private int cacheRefreshIntervalMinutes = 60;
    private String cacheKeyPrefix = "fallback:";
    private boolean cacheWarmingEnabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getCacheTtlHours() {
        return cacheTtlHours;
    }

    public void setCacheTtlHours(int cacheTtlHours) {
        this.cacheTtlHours = cacheTtlHours;
    }

    public int getCacheRefreshIntervalMinutes() {
        return cacheRefreshIntervalMinutes;
    }

    public void setCacheRefreshIntervalMinutes(int cacheRefreshIntervalMinutes) {
        this.cacheRefreshIntervalMinutes = cacheRefreshIntervalMinutes;
    }

    public String getCacheKeyPrefix() {
        return cacheKeyPrefix;
    }

    public void setCacheKeyPrefix(String cacheKeyPrefix) {
        this.cacheKeyPrefix = cacheKeyPrefix;
    }

    public boolean isCacheWarmingEnabled() {
        return cacheWarmingEnabled;
    }

    public void setCacheWarmingEnabled(boolean cacheWarmingEnabled) {
        this.cacheWarmingEnabled = cacheWarmingEnabled;
    }
}
