package com.cvshealth.digital.microservice.iqe.controller;

import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
import com.cvshealth.digital.microservice.iqe.service.EnhancedIQEService;
import com.cvshealth.digital.microservice.iqe.utils.FeatureProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@Slf4j
public class EnhancedIQEController {
    
    private final EnhancedIQEService enhancedIQEService;
    private final FeatureProperties featureProperties;
    
    @QueryMapping
    public Mono<QuestionareRequest> getQuestionnaireEnhanced(@Argument String actionId) {
        if (!featureProperties.isEnhancedRedisCachingEnabled()) {
            return Mono.error(new UnsupportedOperationException("Enhanced Redis caching is disabled"));
        }
        
        QuestionareRequest iqeOutput = new QuestionareRequest();
        return enhancedIQEService.questionnaireByActionIdEnhanced(actionId, iqeOutput);
    }
}
