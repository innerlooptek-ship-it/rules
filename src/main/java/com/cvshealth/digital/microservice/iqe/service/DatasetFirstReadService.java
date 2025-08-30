package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.config.EnhancedRedisConfig;
import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
import com.cvshealth.digital.microservice.iqe.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatasetFirstReadService {
    
    private final EnhancedRedisCacheService redisCacheService;
    private final CassandraService cassandraService;
    private final EnhancedRedisConfig config;
    
    public Mono<QuestionareRequest> getQuestionnaireFromDataset(String actionId) {
        log.debug("Dataset fallback read for actionId: {}", actionId);
        
        return Mono.zip(
            getActionFromDataset(actionId),
            getQuestionsFromDataset(actionId),
            getDetailsFromDataset(actionId)
        ).map(tuple -> {
            ActionsEntity action = tuple.getT1();
            List<QuestionsEntity> questions = tuple.getT2();
            List<QuestionsDetailsEntity> details = tuple.getT3();
            
            if (action == null || action.getActionId() == null) {
                return null;
            }
            
            return buildQuestionnaireFromDataset(action, questions, details);
        }).switchIfEmpty(
            Mono.defer(() -> {
                log.warn("Dataset fallback read FAILED for actionId: {}, no further fallback available", actionId);
                return Mono.empty();
            })
        );
    }
    
    public Mono<List<RulesByFlowEntity>> getRulesByFlowFromDataset(String flow) {
        String key = config.getDatasetKeys().getRulesPrefix() + flow;
        return redisCacheService.getFromCache(key)
            .cast(Map.class)
            .map(rulesMap -> (List<RulesByFlowEntity>) rulesMap.values().stream().toList())
            .onErrorResume(e -> {
                log.warn("Error getting rules from dataset for flow: {}", flow, e);
                return Mono.empty();
            });
    }
    
    private Mono<ActionsEntity> getActionFromDataset(String actionId) {
        return redisCacheService.getFromCache(config.getDatasetKeys().getActionsKey())
            .cast(Map.class)
            .map(actionsMap -> (ActionsEntity) actionsMap.get(actionId))
            .onErrorResume(e -> {
                log.warn("Error getting action from dataset: {}", e.getMessage());
                return Mono.empty();
            });
    }
    
    private Mono<List<QuestionsEntity>> getQuestionsFromDataset(String actionId) {
        String key = config.getDatasetKeys().getQuestionsPrefix() + actionId;
        return redisCacheService.getFromCache(key)
            .cast(Map.class)
            .map(questionsMap -> (List<QuestionsEntity>) questionsMap.values().stream().toList())
            .onErrorResume(e -> {
                log.warn("Error getting questions from dataset: {}", e.getMessage());
                return Mono.empty();
            });
    }
    
    private Mono<List<QuestionsDetailsEntity>> getDetailsFromDataset(String actionId) {
        String key = config.getDatasetKeys().getDetailsPrefix() + actionId;
        return redisCacheService.getFromCache(key)
            .cast(Map.class)
            .map(detailsMap -> (List<QuestionsDetailsEntity>) detailsMap.values().stream().toList())
            .onErrorResume(e -> {
                log.warn("Error getting details from dataset: {}", e.getMessage());
                return Mono.empty();
            });
    }
    
    private QuestionareRequest buildQuestionnaireFromDataset(ActionsEntity action, List<QuestionsEntity> questions, 
                                                           List<QuestionsDetailsEntity> details) {
        QuestionareRequest request = new QuestionareRequest();
        request.setStatusCode("0000");
        
        if (action != null) {
            com.cvshealth.digital.microservice.iqe.dto.Actions actionDto = new com.cvshealth.digital.microservice.iqe.dto.Actions();
            actionDto.setActionId(action.getActionId());
            actionDto.setActionText(action.getActionText());
            request.setActions(actionDto);
        }
        
        return request;
    }
}
