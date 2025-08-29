package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.config.EnhancedRedisConfig;
import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
import com.cvshealth.digital.microservice.iqe.service.strategy.CachingStrategyFactory;
import com.cvshealth.digital.microservice.iqe.utils.FeatureProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnhancedIQEService {
    
    private final ResilientCacheService resilientCacheService;
    private final CachingStrategyFactory strategyFactory;
    private final EnhancedRedisConfig config;
    private final FeatureProperties featureProperties;
    private final IQERepoOrchestrator iqeRepoOrchestrator;
    
    public Mono<QuestionareRequest> questionnaireByActionIdEnhanced(String actionId, QuestionareRequest iqeOutPut) {
        String methodName = "questionnaireByActionIdEnhanced";
        log.debug("ENTRY_LOG {}", methodName);
        
        if (!featureProperties.isEnhancedRedisCachingEnabled()) {
            log.debug("Enhanced Redis caching disabled, falling back to original implementation");
            Map<String, Object> eventMap = new HashMap<>();
            return iqeRepoOrchestrator.getQuestionnaireByActionId(actionId, iqeOutPut, eventMap);
        }
        
        return resilientCacheService.getQuestionnaireWithResilience(actionId)
            .map(result -> {
                iqeOutPut.setActions(result.getActions());
                iqeOutPut.setQuestions(result.getQuestions());
                iqeOutPut.setDetails(result.getDetails());
                iqeOutPut.setStatusCode("0000");
                return iqeOutPut;
            })
            .onErrorResume(e -> {
                log.error("Error in questionnaireByActionIdEnhanced for actionId: {}", actionId, e);
                iqeOutPut.setStatusCode("9999");
                iqeOutPut.setErrorDescription(e.getMessage());
                return Mono.just(iqeOutPut);
            });
    }
}
