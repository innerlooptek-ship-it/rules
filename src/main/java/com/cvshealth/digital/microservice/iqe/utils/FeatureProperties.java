package com.cvshealth.digital.microservice.iqe.utils;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(
    prefix = "feature"
)
public class FeatureProperties {
    private Map<String, Boolean> live;

    public FeatureProperties() {
    }

    public void setLive(final Map<String, Boolean> live) {
        this.live = live;
    }

    public Map<String, Boolean> getLive() {
        return this.live;
    }
    
    public boolean isEnhancedRedisCachingEnabled() {
        return live != null && Boolean.TRUE.equals(live.get("enhanced-redis-caching"));
    }
    
    public boolean isDatasetSnapshotEnabled() {
        return live != null && Boolean.TRUE.equals(live.get("dataset-snapshot-caching"));
    }
    
    public boolean isTableLevelCachingEnabled() {
        return live != null && Boolean.TRUE.equals(live.get("table-level-caching"));
    }
}
