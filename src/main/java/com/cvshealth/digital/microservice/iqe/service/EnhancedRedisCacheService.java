package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.config.EnhancedRedisConfig;
import com.cvshealth.digital.microservice.iqe.dto.*;
import com.cvshealth.digital.microservice.iqe.entity.*;
import com.cvshealth.digital.microservice.iqe.http.DhsHttpConnector;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnhancedRedisCacheService {
    
    private final EnhancedRedisConfig redisConfig;
    private final DhsHttpConnector httpConnector;
    private final ObjectMapper objectMapper;
    
    public Mono<Actions> getActionFromTableCache(String actionId) {
        if (!redisConfig.getTableCaching().isEnabled()) {
            return Mono.empty();
        }
        
        String cacheKey = "questionnaire:action:" + actionId;
        return getFromCache(cacheKey)
            .cast(Actions.class)
            .onErrorResume(e -> {
                log.warn("Error getting action from cache: {}", e.getMessage());
                return Mono.empty();
            });
    }
    
    public Mono<List<Questions>> getQuestionsFromTableCache(String actionId) {
        if (!redisConfig.getTableCaching().isEnabled()) {
            return Mono.empty();
        }
        
        String cacheKey = redisConfig.getTableCaching().getTables().get("questions")
            .getKeyPattern().replace("{actionId}", actionId);
        return getFromCache(cacheKey)
            .cast(List.class)
            .map(list -> (List<Questions>) list)
            .onErrorResume(e -> {
                log.warn("Error getting questions from cache: {}", e.getMessage());
                return Mono.empty();
            });
    }
    
    public Mono<List<RulesByFlowEntity>> getRulesFromTableCache(String flow) {
        if (!redisConfig.getTableCaching().isEnabled()) {
            return Mono.empty();
        }
        
        String cacheKey = redisConfig.getTableCaching().getTables().get("rules_by_flow")
            .getKeyPattern().replace("{flow}", flow);
        return getFromCache(cacheKey)
            .cast(List.class)
            .map(list -> (List<RulesByFlowEntity>) list)
            .onErrorResume(e -> {
                log.warn("Error getting rules from cache: {}", e.getMessage());
                return Mono.empty();
            });
    }
    
    public Mono<QuestionareRequest> getFromTableCache(String actionId) {
        if (!redisConfig.getTableCaching().isEnabled()) {
            return Mono.empty();
        }
        
        return Mono.zip(
            getActionFromTableCache(actionId).defaultIfEmpty(new Actions()),
            getQuestionsFromTableCache(actionId).defaultIfEmpty(List.of()),
            getAnswerOptionsFromTableCache(actionId).defaultIfEmpty(List.of()),
            getDetailsFromTableCache(actionId).defaultIfEmpty(List.of())
        ).map(tuple -> {
            Actions actions = tuple.getT1();
            if (actions.getActionId() == null) {
                return null;
            }
            
            QuestionareRequest request = new QuestionareRequest();
            request.setActions(actions);
            request.setQuestions(tuple.getT2());
            request.setDetails(tuple.getT4());
            request.setStatusCode("0000");
            return request;
        }).filter(request -> request != null);
    }
    
    public Mono<Void> cacheAllActions(List<Actions> actions) {
        if (!redisConfig.getTableCaching().isEnabled()) {
            return Mono.empty();
        }
        
        Map<String, Actions> actionsMap = actions.stream()
            .collect(Collectors.toMap(Actions::getActionId, Function.identity()));
        
        String cacheKey = redisConfig.getTableCaching().getTables().get("actions").getKeyPattern();
        return cacheObject(cacheKey, actionsMap);
    }
    
    public Mono<Void> cacheQuestionnaire(QuestionareRequest questionnaire) {
        if (!redisConfig.getTableCaching().isEnabled() || questionnaire.getActions() == null) {
            return Mono.empty();
        }
        
        String actionId = questionnaire.getActions().getActionId();
        if (actionId == null) {
            return Mono.empty();
        }
        
        return Mono.when(
            cacheAction(actionId, questionnaire.getActions()),
            cacheQuestions(actionId, questionnaire.getQuestions()),
            cacheAnswerOptions(actionId, questionnaire.getQuestions()),
            cacheDetails(actionId, questionnaire.getDetails())
        );
    }
    
    private Mono<Void> cacheAction(String actionId, Actions action) {
        String cacheKey = "questionnaire:action:" + actionId;
        return cacheObject(cacheKey, action);
    }
    
    private Mono<Void> cacheQuestions(String actionId, List<Questions> questions) {
        String cacheKey = redisConfig.getTableCaching().getTables().get("questions")
            .getKeyPattern().replace("{actionId}", actionId);
        return cacheObject(cacheKey, questions);
    }
    
    private Mono<Void> cacheAnswerOptions(String actionId, List<Questions> questions) {
        List<AnswerOptions> allAnswerOptions = questions.stream()
            .filter(q -> q.getAnswerOptions() != null)
            .flatMap(q -> q.getAnswerOptions().stream())
            .collect(Collectors.toList());
        
        String cacheKey = redisConfig.getTableCaching().getTables().get("answer_options")
            .getKeyPattern().replace("{actionId}", actionId);
        return cacheObject(cacheKey, allAnswerOptions);
    }
    
    private Mono<Void> cacheDetails(String actionId, List<Details> details) {
        String cacheKey = redisConfig.getTableCaching().getTables().get("questions_details")
            .getKeyPattern().replace("{actionId}", actionId);
        return cacheObject(cacheKey, details);
    }
    
    public Mono<Void> invalidateTableCache(String table, String key) {
        String cacheKey = redisConfig.getTableCaching().getTables().get(table)
            .getKeyPattern().replace("{actionId}", key).replace("{flow}", key);
        return deleteFromCache(cacheKey);
    }
    
    public Mono<Object> getFromCache(String key) {
        if (!redisConfig.isEnabled()) {
            return Mono.empty();
        }
        
        String redisURL = redisConfig.getBaseUrl() + "?key=" + key + "&cachetype=" + redisConfig.getCacheType();
        return httpConnector.invokeGETService(redisURL, Map.of())
            .flatMap(response -> {
                try {
                    JsonNode jsonNode = objectMapper.readTree(response);
                    return Mono.just(objectMapper.convertValue(jsonNode.get("cacheobject"), Object.class));
                } catch (JsonProcessingException e) {
                    return Mono.error(e);
                }
            });
    }
    
    public Mono<Void> cacheObject(String key, Object object) {
        if (!redisConfig.isEnabled()) {
            return Mono.empty();
        }
        
        try {
            RedisCacheObject cacheObject = new RedisCacheObject(redisConfig.getCacheType(), key, object);
            String jsonRequest = objectMapper.writeValueAsString(cacheObject);
            
            return httpConnector.invokePOSTService(redisConfig.getBaseUrl(), jsonRequest, Map.of())
                .then();
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }
    
    private Mono<Void> deleteFromCache(String key) {
        if (!redisConfig.isEnabled()) {
            return Mono.empty();
        }
        
        String redisURL = redisConfig.getBaseUrl() + "?key=" + key + "&cachetype=" + redisConfig.getCacheType();
        return httpConnector.invokeDELETEService(redisURL, Map.of()).then();
    }
    
    private Mono<List<AnswerOptions>> getAnswerOptionsFromTableCache(String actionId) {
        String cacheKey = redisConfig.getTableCaching().getTables().get("answer_options")
            .getKeyPattern().replace("{actionId}", actionId);
        return getFromCache(cacheKey)
            .cast(List.class)
            .map(list -> (List<AnswerOptions>) list)
            .onErrorResume(e -> {
                log.warn("Error getting answer options from cache: {}", e.getMessage());
                return Mono.empty();
            });
    }
    
    private Mono<List<Details>> getDetailsFromTableCache(String actionId) {
        String cacheKey = redisConfig.getTableCaching().getTables().get("questions_details")
            .getKeyPattern().replace("{actionId}", actionId);
        return getFromCache(cacheKey)
            .cast(List.class)
            .map(list -> (List<Details>) list)
            .onErrorResume(e -> {
                log.warn("Error getting details from cache: {}", e.getMessage());
                return Mono.empty();
            });
    }
    
    private static class RedisCacheObject {
        private String cachetype;
        private String key;
        private Object cacheobject;
        
        public RedisCacheObject(String cachetype, String key, Object cacheobject) {
            this.cachetype = cachetype;
            this.key = key;
            this.cacheobject = cacheobject;
        }
        
        public String getCachetype() { return cachetype; }
        public String getKey() { return key; }
        public Object getCacheobject() { return cacheobject; }
    }
}
