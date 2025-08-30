package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.entity.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimplifiedRedisDataService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String RULES_BY_FLOW_PREFIX = "dataset:rules_by_flow:";
    private static final String ACTIONS_PREFIX = "dataset:actions:";
    private static final String QUESTIONS_PREFIX = "dataset:questions:";
    private static final String ANSWER_OPTIONS_PREFIX = "dataset:answer_options:";
    private static final String QUESTIONS_DETAILS_PREFIX = "dataset:questions_details:";
    private static final String FLOW_RULES_PREFIX = "dataset:flow_rules:";
    
    public Mono<Void> loadCompleteDataset() {
        log.info("Starting complete dataset load into Redis...");
        
        try {
            loadSampleTestData();
            log.info("Complete dataset loaded into Redis successfully with sample data");
        } catch (Exception e) {
            log.error("Failed to load complete dataset into Redis", e);
        }
        return Mono.empty();
    }
    
    
    @SuppressWarnings("unchecked")
    public List<RulesByFlowEntity> getRulesByFlow(String flow) {
        String key = FLOW_RULES_PREFIX + flow;
        log.error("=== REDIS DATA SERVICE - getRulesByFlow START ===");
        log.error("Looking for key: {}", key);
        
        Object cachedData = redisTemplate.opsForValue().get(key);
        log.error("Retrieved cached data: {}", cachedData != null ? cachedData.getClass().getSimpleName() : "NULL");
        
        if (cachedData == null) {
            log.error("No cached data found for flow: {}", flow);
            return null;
        }
        
        try {
            log.error("Cached data type: {}", cachedData.getClass().getName());
            
            if (cachedData instanceof List) {
                List<?> rawList = (List<?>) cachedData;
                log.error("Raw list size: {}", rawList.size());
                List<RulesByFlowEntity> result = new ArrayList<>();
                
                for (int i = 0; i < rawList.size(); i++) {
                    Object item = rawList.get(i);
                    log.error("Item {} type: {}", i, item != null ? item.getClass().getName() : "NULL");
                    
                    if (item instanceof LinkedHashMap) {
                        log.error("Converting LinkedHashMap to RulesByFlowEntity");
                        LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) item;
                        log.error("Map contents: {}", map);
                        RulesByFlowEntity entity = convertMapToRulesByFlowEntity(map);
                        log.error("Converted entity: actionId={}, flow={}, condition={}", 
                            entity.getActionId(), entity.getFlow(), entity.getCondition());
                        result.add(entity);
                    } else if (item instanceof RulesByFlowEntity) {
                        log.error("Item is already RulesByFlowEntity");
                        result.add((RulesByFlowEntity) item);
                    } else {
                        log.error("Unknown item type, skipping: {}", item != null ? item.getClass().getName() : "NULL");
                    }
                }
                
                log.error("Successfully converted {} rules for flow: {}", result.size(), flow);
                log.error("=== REDIS DATA SERVICE - getRulesByFlow END ===");
                return result;
            } else {
                log.error("Cached data is not a List, it's: {}", cachedData.getClass().getName());
            }
        } catch (Exception e) {
            log.error("Error converting cached data for flow: {}", flow, e);
        }
        
        log.error("=== REDIS DATA SERVICE - getRulesByFlow END (NULL) ===");
        return null;
    }
    
    public RulesByFlowEntity getRuleByActionId(String actionId) {
        String key = RULES_BY_FLOW_PREFIX + actionId;
        Object cachedData = redisTemplate.opsForValue().get(key);
        
        if (cachedData == null) {
            return null;
        }
        
        if (cachedData instanceof LinkedHashMap) {
            return convertMapToRulesByFlowEntity((LinkedHashMap<String, Object>) cachedData);
        } else if (cachedData instanceof RulesByFlowEntity) {
            return (RulesByFlowEntity) cachedData;
        }
        
        return null;
    }
    
    public ActionsEntity getActionByActionId(String actionId) {
        String key = ACTIONS_PREFIX + actionId;
        Object cachedData = redisTemplate.opsForValue().get(key);
        
        if (cachedData == null) {
            return null;
        }
        
        if (cachedData instanceof LinkedHashMap) {
            return convertMapToActionsEntity((LinkedHashMap<String, Object>) cachedData);
        } else if (cachedData instanceof ActionsEntity) {
            return (ActionsEntity) cachedData;
        }
        
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public List<QuestionsEntity> getQuestionsByActionId(String actionId) {
        String key = QUESTIONS_PREFIX + actionId;
        Object cachedData = redisTemplate.opsForValue().get(key);
        
        if (cachedData == null) {
            return null;
        }
        
        if (cachedData instanceof List) {
            List<?> rawList = (List<?>) cachedData;
            List<QuestionsEntity> result = new ArrayList<>();
            
            for (Object item : rawList) {
                if (item instanceof LinkedHashMap) {
                    result.add(convertMapToQuestionsEntity((LinkedHashMap<String, Object>) item));
                } else if (item instanceof QuestionsEntity) {
                    result.add((QuestionsEntity) item);
                }
            }
            return result;
        }
        
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public List<AnswerOptionsEntity> getAnswerOptionsByActionId(String actionId) {
        String key = ANSWER_OPTIONS_PREFIX + actionId;
        Object cachedData = redisTemplate.opsForValue().get(key);
        
        if (cachedData == null) {
            return null;
        }
        
        if (cachedData instanceof List) {
            List<?> rawList = (List<?>) cachedData;
            List<AnswerOptionsEntity> result = new ArrayList<>();
            
            for (Object item : rawList) {
                if (item instanceof LinkedHashMap) {
                    result.add(convertMapToAnswerOptionsEntity((LinkedHashMap<String, Object>) item));
                } else if (item instanceof AnswerOptionsEntity) {
                    result.add((AnswerOptionsEntity) item);
                }
            }
            return result;
        }
        
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public List<QuestionsDetailsEntity> getQuestionDetailsByActionId(String actionId) {
        String key = QUESTIONS_DETAILS_PREFIX + actionId;
        Object cachedData = redisTemplate.opsForValue().get(key);
        
        if (cachedData == null) {
            return null;
        }
        
        if (cachedData instanceof List) {
            List<?> rawList = (List<?>) cachedData;
            List<QuestionsDetailsEntity> result = new ArrayList<>();
            
            for (Object item : rawList) {
                if (item instanceof LinkedHashMap) {
                    result.add(convertMapToQuestionsDetailsEntity((LinkedHashMap<String, Object>) item));
                } else if (item instanceof QuestionsDetailsEntity) {
                    result.add((QuestionsDetailsEntity) item);
                }
            }
            return result;
        }
        
        return null;
    }
    
    private RulesByFlowEntity convertMapToRulesByFlowEntity(LinkedHashMap<String, Object> map) {
        RulesByFlowEntity entity = new RulesByFlowEntity();
        
        if (map.get("flow") != null) entity.setFlow((String) map.get("flow"));
        if (map.get("ruleId") != null) entity.setRuleId((String) map.get("ruleId"));
        if (map.get("ruleName") != null) entity.setRuleName((String) map.get("ruleName"));
        if (map.get("actionId") != null) entity.setActionId((String) map.get("actionId"));
        if (map.get("condition") != null) entity.setCondition((String) map.get("condition"));
        if (map.get("lob") != null) entity.setLob((String) map.get("lob"));
        if (map.get("salience") != null) entity.setSalience((Integer) map.get("salience"));
        if (map.get("active") != null) entity.setActive((Boolean) map.get("active"));
        
        return entity;
    }
    
    @SuppressWarnings("unchecked")
    private ActionsEntity convertMapToActionsEntity(LinkedHashMap<String, Object> map) {
        ActionsEntity entity = new ActionsEntity();
        
        if (map.get("actionId") != null) entity.setActionId((String) map.get("actionId"));
        if (map.get("actionText") != null) entity.setActionText((String) map.get("actionText"));
        if (map.get("questionId") != null) entity.setQuestionId((List<String>) map.get("questionId"));
        if (map.get("detailId") != null) entity.setDetailId((List<String>) map.get("detailId"));
        
        return entity;
    }
    
    @SuppressWarnings("unchecked")
    private QuestionsEntity convertMapToQuestionsEntity(LinkedHashMap<String, Object> map) {
        QuestionsEntity entity = new QuestionsEntity();
        
        if (map.get("actionId") != null) entity.setActionId((String) map.get("actionId"));
        if (map.get("questionId") != null) entity.setQuestionId((String) map.get("questionId"));
        if (map.get("questionText") != null) entity.setQuestionText((String) map.get("questionText"));
        if (map.get("errorMessage") != null) entity.setErrorMessage((String) map.get("errorMessage"));
        if (map.get("answerType") != null) entity.setAnswerType((String) map.get("answerType"));
        if (map.get("answerOptionId") != null) entity.setAnswerOptionId((List<String>) map.get("answerOptionId"));
        if (map.get("helpText") != null) entity.setHelpText((String) map.get("helpText"));
        if (map.get("characterLimit") != null) {
            Object value = map.get("characterLimit");
            if (value instanceof Integer) {
                entity.setCharacterLimit((Integer) value);
            } else if (value instanceof Number) {
                entity.setCharacterLimit(((Number) value).intValue());
            }
        }
        if (map.get("stacked") != null) entity.setStacked((Boolean) map.get("stacked"));
        if (map.get("sequence_id") != null) entity.setSequence_id((Integer) map.get("sequence_id"));
        if (map.get("required") != null) entity.setRequired((Boolean) map.get("required"));
        if (map.get("linkText") != null) entity.setLinkText((String) map.get("linkText"));
        if (map.get("questionnumber") != null) entity.setQuestionnumber((Integer) map.get("questionnumber"));
        if (map.get("skiplegend") != null) entity.setSkiplegend((String) map.get("skiplegend"));
        if (map.get("subcontext") != null) entity.setSubcontext((String) map.get("subcontext"));
        
        return entity;
    }
    
    @SuppressWarnings("unchecked")
    private AnswerOptionsEntity convertMapToAnswerOptionsEntity(LinkedHashMap<String, Object> map) {
        AnswerOptionsEntity entity = new AnswerOptionsEntity();
        
        if (map.get("actionId") != null) entity.setActionId((String) map.get("actionId"));
        if (map.get("questionId") != null) entity.setQuestionId((String) map.get("questionId"));
        if (map.get("answerOptionId") != null) entity.setAnswerOptionId((String) map.get("answerOptionId"));
        if (map.get("answerText") != null) entity.setAnswerText((String) map.get("answerText"));
        if (map.get("answerValue") != null) entity.setAnswerValue((String) map.get("answerValue"));
        if (map.get("sequence_id") != null) entity.setSequence_id((Integer) map.get("sequence_id"));
        if (map.get("additionalDetailText") != null) entity.setAdditionalDetailText((String) map.get("additionalDetailText"));
        if (map.get("relatedQuestions") != null) entity.setRelatedQuestions((List<String>) map.get("relatedQuestions"));
        
        return entity;
    }
    
    private QuestionsDetailsEntity convertMapToQuestionsDetailsEntity(LinkedHashMap<String, Object> map) {
        QuestionsDetailsEntity entity = new QuestionsDetailsEntity();
        
        if (map.get("actionId") != null) entity.setActionId((String) map.get("actionId"));
        if (map.get("detailId") != null) entity.setDetailId((String) map.get("detailId"));
        if (map.get("title") != null) entity.setTitle((String) map.get("title"));
        if (map.get("instructions") != null) entity.setInstructions((String) map.get("instructions"));
        if (map.get("helper") != null) entity.setHelper((String) map.get("helper"));
        if (map.get("subContext") != null) entity.setSubContext((String) map.get("subContext"));
        if (map.get("pageNumber") != null) entity.setPageNumber((Integer) map.get("pageNumber"));
        if (map.get("footer") != null) entity.setFooter((String) map.get("footer"));
        if (map.get("sequenceId") != null) entity.setSequenceId((Integer) map.get("sequenceId"));
        
        return entity;
    }
    
    /**
     * Load sample test data into Redis for testing when Cassandra is unavailable
     */
    public void loadSampleTestData() {
        log.info("Loading sample test data into Redis for testing...");
        
        try {
            RulesByFlowEntity sampleRule = new RulesByFlowEntity();
            sampleRule.setFlow("VACCINE");
            sampleRule.setRuleId("test-rule-1");
            sampleRule.setRuleName("Test Vaccine Rule");
            sampleRule.setActionId("test-action-123");
            sampleRule.setCondition("requiredQuestionnaireContext==\"MC_VACCINE_PATIENT_PROFILE_INFO\"");
            sampleRule.setLob("VACCINE");
            sampleRule.setSalience(100);
            sampleRule.setActive(true);
            
            String ruleKey = RULES_BY_FLOW_PREFIX + sampleRule.getActionId();
            redisTemplate.opsForValue().set(ruleKey, sampleRule, Duration.ofHours(24));
            
            String flowKey = FLOW_RULES_PREFIX + sampleRule.getFlow();
            redisTemplate.opsForValue().set(flowKey, List.of(sampleRule), Duration.ofHours(24));
            
            ActionsEntity sampleAction = new ActionsEntity();
            sampleAction.setActionId("test-action-123");
            sampleAction.setActionText("Test vaccine questionnaire");
            sampleAction.setQuestionId(List.of("race", "ethnicity"));
            sampleAction.setDetailId(List.of("detail-1"));
            
            String actionKey = ACTIONS_PREFIX + sampleAction.getActionId();
            redisTemplate.opsForValue().set(actionKey, sampleAction, Duration.ofHours(24));
            
            log.info("Sample test data loaded successfully into Redis");
            
        } catch (Exception e) {
            log.error("Failed to load sample test data into Redis", e);
        }
    }
}
