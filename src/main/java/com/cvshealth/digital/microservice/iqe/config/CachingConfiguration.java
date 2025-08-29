package com.cvshealth.digital.microservice.iqe.config;

import com.cvshealth.digital.microservice.iqe.service.strategy.CachingStrategy;
import com.cvshealth.digital.microservice.iqe.service.strategy.CachingStrategyFactory;
import com.cvshealth.digital.microservice.iqe.utils.FeatureProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CachingConfiguration {
    
    private final CachingStrategyFactory strategyFactory;
    private final EnhancedRedisConfig redisConfig;
    private final FeatureProperties featureProperties;
    
    @Bean
    @ConditionalOnProperty(name = "feature.live.enhanced-redis-caching", havingValue = "true")
    public CachingStrategy activeCachingStrategy() {
        return strategyFactory.getStrategy(redisConfig.getStrategyEnum());
    }
}
