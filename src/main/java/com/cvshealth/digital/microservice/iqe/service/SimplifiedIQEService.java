package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.dto.*;
import com.cvshealth.digital.microservice.iqe.dto.IQEMcCoreQuestionnarieRequest;
import com.cvshealth.digital.microservice.iqe.entity.*;
import com.cvshealth.digital.microservice.iqe.model.RulesDetails;
import com.cvshealth.digital.microservice.iqe.config.DroolConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.drools.template.ObjectDataCompiler;
import org.kie.api.KieServices;
import org.kie.api.KieBase;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.internal.utils.KieHelper;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SimplifiedIQEService {
    
    private final SimplifiedRedisDataService redisDataService;
    
    public SimplifiedIQEService(SimplifiedRedisDataService redisDataService) {
        this.redisDataService = redisDataService;
        log.error("=== SimplifiedIQEService CONSTRUCTOR CALLED ===");
        log.error("RedisDataService injected: {}", redisDataService != null ? "SUCCESS" : "FAILED");
    }
    
    public Mono<QuestionareRequest> dynamicFlowConditionEvaluation(IQEMcCoreQuestionnarieRequest request) {
        log.error("=== SIMPLIFIED REDIS SERVICE START ===");
        log.error("Processing questionnaire request using Redis data: {}", request);
        log.error("RedisDataService instance: {}", redisDataService != null ? "NOT NULL" : "NULL");
        
        return Mono.fromCallable(() -> {
            log.error("Inside Mono.fromCallable");
            String flow = request.getFlow();
            log.error("Request flow: {}", flow);
            log.error("Request context: {}", request.getRequiredQuestionnaireContext());
            
            if (flow == null) {
                log.error("No flow found in request: {}", request);
                return new QuestionareRequest();
            }
            
            log.error("Calling redisDataService.getRulesByFlow({})", flow);
            List<RulesByFlowEntity> rules = redisDataService.getRulesByFlow(flow);
            log.error("Retrieved {} rules from Redis for flow: {}", rules != null ? rules.size() : 0, flow);
            
            if (rules == null || rules.isEmpty()) {
                log.error("No rules found in Redis for flow: {}", flow);
                return new QuestionareRequest();
            }
            
            log.error("Executing rules engine with {} rules", rules.size());
            String actionId = executeRulesEngine(rules, request);
            log.error("Rules engine returned actionId: {}", actionId);
            
            if (actionId == null || actionId.isEmpty()) {
                log.error("No actionId determined for request: {}", request);
                return new QuestionareRequest();
            }
            
            log.error("Building questionnaire response for actionId: {}", actionId);
            QuestionareRequest response = buildQuestionnaireResponse(actionId);
            log.error("Built response with {} questions", response.getQuestions() != null ? response.getQuestions().size() : 0);
            log.error("=== SIMPLIFIED REDIS SERVICE END ===");
            return response;
        }).doOnError(e -> log.error("Error in SimplifiedIQEService", e));
    }
    
    private String executeRulesEngine(List<RulesByFlowEntity> rules, IQEMcCoreQuestionnarieRequest request) {
        try {
            log.error("Executing simplified rules engine with {} rules for request: {}", rules.size(), request);
            
            if (rules == null || rules.isEmpty()) {
                log.error("Rules list is null or empty");
                return null;
            }
            
            for (RulesByFlowEntity rule : rules) {
                if (rule == null) {
                    log.error("Null rule encountered, skipping");
                    continue;
                }
                
                log.error("Evaluating rule: {} with condition: {}", rule.getRuleName(), rule.getCondition());
                log.error("Rule details - ActionId: {}, Flow: {}, Active: {}", rule.getActionId(), rule.getFlow(), rule.isActive());
                
                if (evaluateRuleCondition(rule.getCondition(), request)) {
                    log.error("Rule matched! ActionId: {}", rule.getActionId());
                    return rule.getActionId();
                } else {
                    log.error("Rule did NOT match for condition: {}", rule.getCondition());
                }
            }
            
            log.error("No rules matched for request: {}", request);
            return null;
            
        } catch (Exception e) {
            log.error("Error executing simplified rules engine", e);
            return null;
        }
    }
    
    private boolean evaluateRuleCondition(String condition, IQEMcCoreQuestionnarieRequest request) {
        if (condition == null || condition.trim().isEmpty()) {
            log.error("Rule condition is null or empty");
            return false;
        }
        
        try {
            log.error("Evaluating condition: '{}'", condition);
            log.error("Request context: '{}'", request.getRequiredQuestionnaireContext());
            
            if (condition.contains("requiredQuestionnaireContext")) {
                String expectedContext = extractQuotedValue(condition, "requiredQuestionnaireContext");
                boolean matches = expectedContext != null && expectedContext.equals(request.getRequiredQuestionnaireContext());
                log.error("Condition check: expected='{}', actual='{}', matches={}", 
                    expectedContext, request.getRequiredQuestionnaireContext(), matches);
                return matches;
            } else {
                log.error("Condition does not contain 'requiredQuestionnaireContext': {}", condition);
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("Error evaluating rule condition: {}", condition, e);
            return false;
        }
    }
    
    private String extractQuotedValue(String condition, String fieldName) {
        try {
            String pattern = fieldName + "\\s*==\\s*\"([^\"]+)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(condition);
            if (m.find()) {
                return m.group(1);
            }
            return null;
        } catch (Exception e) {
            log.error("Error extracting quoted value from condition: {}", condition, e);
            return null;
        }
    }
    
    private QuestionareRequest buildQuestionnaireResponse(String actionId) {
        try {
            RulesByFlowEntity rulesByFlow = redisDataService.getRuleByActionId(actionId);
            ActionsEntity actions = redisDataService.getActionByActionId(actionId);
            List<QuestionsEntity> questions = redisDataService.getQuestionsByActionId(actionId);
            List<AnswerOptionsEntity> answerOptions = redisDataService.getAnswerOptionsByActionId(actionId);
            List<QuestionsDetailsEntity> details = redisDataService.getQuestionDetailsByActionId(actionId);
            
            if (rulesByFlow == null || actions == null) {
                log.warn("Missing core data for actionId: {}", actionId);
                return new QuestionareRequest();
            }
            
            QuestionareRequest response = new QuestionareRequest();
            
            RulesByFlow rulesByFlowDto = RulesByFlow.builder()
                .flow(rulesByFlow.getFlow())
                .ruleId(rulesByFlow.getRuleId())
                .ruleName(rulesByFlow.getRuleName())
                .actionId(rulesByFlow.getActionId())
                .condition(rulesByFlow.getCondition())
                .lob(rulesByFlow.getLob())
                .salience(rulesByFlow.getSalience())
                .audit(convertAudit(rulesByFlow.getAudit()))
                .isActive(rulesByFlow.isActive())
                .build();
            response.setRulesByFlow(rulesByFlowDto);
            
            Actions actionsDto = Actions.builder()
                .actionId(actions.getActionId())
                .actionText(actions.getActionText())
                .questionIds(actions.getQuestionId())
                .detailIds(actions.getDetailId())
                .build();
            response.setActions(actionsDto);
            
            if (questions != null && !questions.isEmpty()) {
                List<Questions> questionsDto = questions.stream()
                    .map(question -> {
                        List<AnswerOptions> questionAnswerOptions = answerOptions != null ? 
                            answerOptions.stream()
                                .filter(ao -> ao.getQuestionId().equals(question.getQuestionId()))
                                .map(this::convertAnswerOption)
                                .sorted(Comparator.comparingInt(AnswerOptions::getSequenceId))
                                .collect(Collectors.toList()) : new ArrayList<>();
                        
                        return Questions.builder()
                            .actionId(question.getActionId())
                            .questionId(question.getQuestionId())
                            .text(question.getQuestionText())
                            .errorMessage(question.getErrorMessage())
                            .answerType(question.getAnswerType())
                            .answerOptionIds(question.getAnswerOptionId())
                            .answerOptions(questionAnswerOptions)
                            .helpText(question.getHelpText())
                            .characterLimit(question.getCharacterLimit())
                            .stacked(question.isStacked())
                            .sequenceId(question.getSequence_id())
                            .required(question.isRequired())
                            .linkText(question.getLinkText())
                            .questionNumber(question.getQuestionnumber())
                            .skipLegend(question.getSkiplegend())
                            .subContext(question.getSubcontext())
                            .build();
                    })
                    .sorted(Comparator.comparingInt(Questions::getSequenceId))
                    .collect(Collectors.toList());
                
                response.setQuestions(questionsDto);
            }
            
            if (details != null && !details.isEmpty()) {
                List<Details> detailsDto = details.stream()
                    .map(this::convertDetail)
                    .sorted(Comparator.comparingInt(Details::getSequenceId))
                    .collect(Collectors.toList());
                response.setDetails(detailsDto);
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("Error building questionnaire response for actionId: {}", actionId, e);
            return new QuestionareRequest();
        }
    }
    
    private AnswerOptions convertAnswerOption(AnswerOptionsEntity entity) {
        return AnswerOptions.builder()
            .actionId(entity.getActionId())
            .questionId(entity.getQuestionId())
            .answerOptionId(entity.getAnswerOptionId())
            .text(entity.getAnswerText())
            .value(entity.getAnswerValue())
            .sequenceId(entity.getSequence_id())
            .additionalDetailText(entity.getAdditionalDetailText())
            .relatedQuestionIds(entity.getRelatedQuestions())
            .build();
    }
    
    private Details convertDetail(QuestionsDetailsEntity entity) {
        return Details.builder()
            .actionId(entity.getActionId())
            .detailId(entity.getDetailId())
            .title(entity.getTitle())
            .instructions(entity.getInstructions())
            .helper(entity.getHelper())
            .subContext(entity.getSubContext())
            .pageNumber(entity.getPageNumber())
            .footer(entity.getFooter())
            .sequenceId(entity.getSequenceId())
            .build();
    }
    
    private Audit convertAudit(com.cvshealth.digital.microservice.iqe.udt.AuditEntity auditEntity) {
        if (auditEntity == null) return null;
        
        return Audit.builder()
            .createdTs(auditEntity.getCreatedTs())
            .createdBy(auditEntity.getCreatedBy())
            .modifiedTs(auditEntity.getModifiedTs())
            .modifiedBy(auditEntity.getModifiedBy())
            .remarks(auditEntity.getRemarks())
            .build();
    }
}
