package com.cvshealth.digital.microservice.iqe.service.strategy;

import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
import reactor.core.publisher.Mono;

public interface CachingStrategy {
    Mono<QuestionareRequest> getQuestionnaire(String actionId);
    Mono<Void> invalidateCache(String actionId);
    Mono<Void> refreshCache(String actionId);
}
