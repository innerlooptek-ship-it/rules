package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.config.EnhancedRedisConfig;
import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheValidationService {
    
    private final EnhancedRedisConfig config;
    
    public boolean isValidCacheKey(String key) {
        return key != null && !key.trim().isEmpty() && key.length() <= 250;
    }
    
    public boolean isValidQuestionnaireData(QuestionareRequest questionnaire) {
        if (questionnaire == null) {
            return false;
        }
        
        if (questionnaire.getActions() == null || 
            questionnaire.getActions().getActionId() == null) {
            log.warn("Invalid questionnaire data: missing actions or actionId");
            return false;
        }
        
        return true;
    }
    
    public String buildCacheKey(String table, String identifier) {
        if (!config.getTableCaching().isEnabled()) {
            return null;
        }
        
        var tableConfig = config.getTableCaching().getTables().get(table);
        if (tableConfig == null || !tableConfig.isEnabled()) {
            return null;
        }
        
        String keyPattern = tableConfig.getKeyPattern();
        return keyPattern.replace("{actionId}", identifier)
                        .replace("{flow}", identifier);
    }
    
    public boolean isTableCachingEnabled(String table) {
        if (!config.getTableCaching().isEnabled()) {
            return false;
        }
        
        var tableConfig = config.getTableCaching().getTables().get(table);
        return tableConfig != null && tableConfig.isEnabled();
    }
}
