package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
import com.cvshealth.digital.microservice.iqe.dto.RelatedQuestionsRequest;
import com.cvshealth.digital.microservice.iqe.entity.ActionsEntity;
import com.cvshealth.digital.microservice.iqe.entity.AnswerOptionsEntity;
import com.cvshealth.digital.microservice.iqe.entity.QuestionsEntity;
import com.cvshealth.digital.microservice.iqe.entity.RulesByFlowEntity;
import com.cvshealth.digital.microservice.iqe.entity.QuestionnaireRules;
import com.cvshealth.digital.microservice.iqe.model.Questions;
import com.cvshealth.digital.microservice.iqe.model.RulesDetails;
import com.cvshealth.digital.microservice.iqe.repository.ActionsRepository;
import com.cvshealth.digital.microservice.iqe.repository.AnswerOptionsRepository;
import com.cvshealth.digital.microservice.iqe.repository.QuestionsRepository;
import com.cvshealth.digital.microservice.iqe.repository.QuestionnaireDetailsRepository;
import com.cvshealth.digital.microservice.iqe.repository.QuestionnaireRulesRepository;
import com.cvshealth.digital.microservice.iqe.repository.RulesByFlowRepository;
import com.cvshealth.digital.microservice.iqe.service.RedisCacheService;
import com.cvshealth.digital.microservice.iqe.service.RulesServiceRepoOrchestrator;
import com.cvshealth.digital.microservice.iqe.testdata.TestDataBuilder;
import com.cvshealth.digital.microservice.iqe.utils.LoggingUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IQEServiceUnitTest {
    
    @Mock
    private ActionsRepository actionsRepository;
    
    @Mock
    private QuestionsRepository questionsRepository;
    
    @Mock
    private RulesByFlowRepository rulesByFlowRepository;
    
    @Mock
    private QuestionnaireRulesRepository rulesRepository;
    
    @Mock
    private AnswerOptionsRepository answerOptionsRepository;
    
    @Mock
    private QuestionnaireDetailsRepository questionnaireDetailsRepository;
    
    @Mock
    private IQERepoOrchestrator iqeRepoOrchestrator;
    
    @Mock
    private RulesServiceRepoOrchestrator rulesServiceRepoOrchestrator;
    
    @Mock
    private RedisCacheService redisCacheService;
    
    @Mock
    private LoggingUtils loggingUtils;
    
    @InjectMocks
    private IQEService iqeService;
    
    @BeforeEach
    void setUp() {
    }
    
    @Test
    void shouldDeleteQuestionnaireByActionIdSuccessfully() {
        RulesByFlowEntity rule = TestDataBuilder.createTestRule();
        when(rulesByFlowRepository.findByActionId(anyString())).thenReturn(Flux.just(rule));
        when(rulesByFlowRepository.deleteByFlowAndRuleId(anyString(), anyString())).thenReturn(Mono.empty());
        when(actionsRepository.deleteByActionId(anyString())).thenReturn(Mono.empty());
        when(questionsRepository.deleteByActionId(anyString())).thenReturn(Mono.empty());
        when(answerOptionsRepository.deleteByActionId(anyString())).thenReturn(Mono.empty());
        when(questionnaireDetailsRepository.deleteByActionId(anyString())).thenReturn(Mono.empty());
        when(redisCacheService.deleteDataFromRedis(anyString(), anyString(), any(Map.class))).thenReturn(Mono.empty());
        
        StepVerifier.create(iqeService.deleteQuestionnaireByActionId("test-action-id"))
                .expectNextMatches(result -> result != null && "0000".equals(result.getStatusCode()))
                .verifyComplete();
    }
    
    @Test
    void shouldHandleEmptyRulesGracefully() {
        RulesDetails rulesDetails = TestDataBuilder.createTestRulesDetails();
        Map<String, String> headers = new HashMap<>();
        Map<String, Object> reqHeaders = new HashMap<>();
        
        when(rulesRepository.findByFlow(anyString())).thenReturn(Flux.empty());
        
        StepVerifier.create(iqeService.getRuleDetails(rulesDetails, headers, reqHeaders))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldGetQuestionnaireByActionId() {
        String actionId = "test-action-id";
        QuestionareRequest iqeOutput = new QuestionareRequest();
        
        when(redisCacheService.getDataFromRedis(anyString(), anyString(), any(Map.class))).thenReturn(Mono.empty());
        when(rulesByFlowRepository.findByActionId(actionId)).thenReturn(Flux.empty());
        
        StepVerifier.create(iqeService.questionnaireByActionId(actionId, iqeOutput))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleEmptyActionIdInDelete() {
        when(rulesByFlowRepository.findByActionId("")).thenReturn(Flux.empty());
        
        StepVerifier.create(iqeService.deleteQuestionnaireByActionId(""))
                .expectNextMatches(result -> result != null && "5009".equals(result.getStatusCode()) && "ActionId not found".equals(result.getActionId()))
                .verifyComplete();
    }
    
    @Test
    void shouldHandleRepositoryExceptionsInDelete() {
        when(rulesByFlowRepository.findByActionId("test-action-id"))
                .thenReturn(Flux.error(new RuntimeException("Database error")));
        
        StepVerifier.create(iqeService.deleteQuestionnaireByActionId("test-action-id"))
                .expectError(RuntimeException.class)
                .verify();
    }
    
    @Test
    void shouldGetAllRulesSuccessfully() {
        RulesByFlowEntity activeRule = TestDataBuilder.createTestRule();
        activeRule.setActive(true);
        RulesByFlowEntity inactiveRule = TestDataBuilder.createTestRule();
        inactiveRule.setActive(false);
        
        when(rulesByFlowRepository.findAll()).thenReturn(Flux.just(activeRule, inactiveRule));
        
        StepVerifier.create(iqeService.rules())
                .expectNextMatches(result -> 
                    result != null && 
                    result.getActiveRules() != null && 
                    result.getInactiveRules() != null &&
                    result.getActiveRules().size() == 1 &&
                    result.getInactiveRules().size() == 1)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleQuestionnaireByFlowAndCondition() {
        RulesDetails rulesDetails = TestDataBuilder.createTestRulesDetails();
        rulesDetails.setFlow("test-flow");
        rulesDetails.setActionId("test-action-id");
        
        QuestionareRequest iqeOutput = new QuestionareRequest();
        Map<String, String> headers = new HashMap<>();
        
        RulesByFlowEntity rule = TestDataBuilder.createTestRule();
        when(rulesByFlowRepository.findByFlow("test-flow")).thenReturn(Flux.just(rule));
        when(redisCacheService.getDataFromRedis(anyString(), anyString(), any(Map.class))).thenReturn(Mono.empty());
        when(rulesByFlowRepository.findByActionId(anyString())).thenReturn(Flux.empty());
        
        StepVerifier.create(iqeService.questionnaireByFlowAndCondition(rulesDetails, iqeOutput, headers))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleQuestionnaireByActionAndQuestionId() {
        String actionId = "test-action-id";
        String questionId = "test-question-id";
        Map<String, String> headers = new HashMap<>();
        
        QuestionsEntity question = TestDataBuilder.createTestQuestion();
        AnswerOptionsEntity answerOption = TestDataBuilder.createTestAnswerOption();
        
        when(questionsRepository.findByActionIdAndQuestionId(actionId, questionId))
                .thenReturn(Mono.just(question));
        when(answerOptionsRepository.findByActionIdAndQuestionId(actionId, questionId))
                .thenReturn(Flux.just(answerOption));
        
        StepVerifier.create(iqeService.questionnaireByActionAndQuestionId(actionId, questionId, headers))
                .expectNextMatches(result -> 
                    result != null && 
                    result.getQuestion() != null)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleQuestionnaireByFlow() {
        RulesDetails rulesDetails = TestDataBuilder.createTestRulesDetails();
        rulesDetails.setFlow("test-flow");
        rulesDetails.setActionId("test-action-id");
        
        QuestionareRequest iqeOutput = new QuestionareRequest();
        Map<String, String> headers = new HashMap<>();
        
        RulesByFlowEntity rule = TestDataBuilder.createTestRule();
        ActionsEntity action = TestDataBuilder.createTestAction();
        action.setQuestionId(List.of("question-1"));
        
        when(rulesByFlowRepository.findByFlow("test-flow")).thenReturn(Flux.just(rule));
        when(actionsRepository.findByActionId(anyString())).thenReturn(Flux.just(action));
        when(questionsRepository.findByActionIdAndQuestionId(anyString(), anyString()))
                .thenReturn(Mono.just(TestDataBuilder.createTestQuestion()));
        when(answerOptionsRepository.findByActionIdAndQuestionId(anyString(), anyString()))
                .thenReturn(Flux.just(TestDataBuilder.createTestAnswerOption()));
        
        StepVerifier.create(iqeService.questionnaireByFlow(rulesDetails, iqeOutput, headers))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleGetQuestionsByRelatedQuestionsList() {
        RelatedQuestionsRequest relatedQuestions = new RelatedQuestionsRequest();
        com.cvshealth.digital.microservice.iqe.dto.Questions question = TestDataBuilder.createTestQuestionDto();
        relatedQuestions.setRelatedQuestions(List.of(question));
        
        QuestionareRequest iqeOutput = new QuestionareRequest();
        Map<String, String> headers = new HashMap<>();
        
        when(questionsRepository.findByActionIdAndQuestionId(anyString(), anyString()))
                .thenReturn(Mono.just(TestDataBuilder.createTestQuestion()));
        when(answerOptionsRepository.findByActionIdAndQuestionId(anyString(), anyString()))
                .thenReturn(Flux.just(TestDataBuilder.createTestAnswerOption()));
        
        StepVerifier.create(iqeService.getQuestionsByRelatedQuestionsList(relatedQuestions, iqeOutput, headers))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleEmptyRelatedQuestionsList() {
        RelatedQuestionsRequest relatedQuestions = new RelatedQuestionsRequest();
        relatedQuestions.setRelatedQuestions(List.of());
        
        QuestionareRequest iqeOutput = new QuestionareRequest();
        Map<String, String> headers = new HashMap<>();
        
        StepVerifier.create(iqeService.getQuestionsByRelatedQuestionsList(relatedQuestions, iqeOutput, headers))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleRulesWithDroolsExecution() {
        RulesDetails rulesDetails = TestDataBuilder.createTestRulesDetails();
        rulesDetails.setFlow("test-flow");
        
        Map<String, String> headers = new HashMap<>();
        Map<String, Object> reqHeaders = new HashMap<>();
        
        QuestionnaireRules rule = TestDataBuilder.createTestQuestionnaireRule();
        when(rulesRepository.findByFlow("test-flow")).thenReturn(Flux.just(rule));
        
        StepVerifier.create(iqeService.getRuleDetails(rulesDetails, headers, reqHeaders))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleNullRulesDetailsGracefully() {
        Map<String, String> headers = new HashMap<>();
        Map<String, Object> reqHeaders = new HashMap<>();
        
        when(rulesRepository.findByFlow(anyString())).thenReturn(Flux.empty());
        
        RulesDetails rulesDetails = new RulesDetails();
        rulesDetails.setFlow("test-flow");
        
        StepVerifier.create(iqeService.getRuleDetails(rulesDetails, headers, reqHeaders))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleQuestionnaireByActionIdWithCaching() {
        String actionId = "test-action-id";
        QuestionareRequest iqeOutput = new QuestionareRequest();
        QuestionareRequest cachedResponse = new QuestionareRequest();
        
        when(redisCacheService.getDataFromRedis(anyString(), anyString(), any(Map.class)))
                .thenReturn(Mono.just(cachedResponse));
        
        StepVerifier.create(iqeService.questionnaireByActionId(actionId, iqeOutput))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleQuestionnaireByFlowWithActionProcessing() {
        RulesDetails rulesDetails = TestDataBuilder.createTestRulesDetails();
        rulesDetails.setFlow("test-flow");
        rulesDetails.setActionId("test-action-id");
        
        QuestionareRequest iqeOutput = new QuestionareRequest();
        Map<String, String> headers = new HashMap<>();
        
        RulesByFlowEntity rule = TestDataBuilder.createTestRule();
        ActionsEntity action = TestDataBuilder.createTestAction();
        action.setQuestionId(List.of("question-1"));
        
        when(rulesByFlowRepository.findByFlow("test-flow")).thenReturn(Flux.just(rule));
        when(actionsRepository.findByActionId(anyString())).thenReturn(Flux.just(action));
        when(questionsRepository.findByActionIdAndQuestionId(anyString(), anyString()))
                .thenReturn(Mono.just(TestDataBuilder.createTestQuestion()));
        when(answerOptionsRepository.findByActionIdAndQuestionId(anyString(), anyString()))
                .thenReturn(Flux.just(TestDataBuilder.createTestAnswerOption()));
        
        StepVerifier.create(iqeService.questionnaireByFlow(rulesDetails, iqeOutput, headers))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleQuestionnaireByFlowAndConditionWithDroolsExecution() {
        RulesDetails rulesDetails = TestDataBuilder.createTestRulesDetails();
        rulesDetails.setFlow("test-flow");
        rulesDetails.setRequiredQuestionnaireContext("test-context");
        
        QuestionareRequest iqeOutput = new QuestionareRequest();
        Map<String, String> headers = new HashMap<>();
        
        RulesByFlowEntity rule = TestDataBuilder.createTestRule();
        rule.setCondition("requiredQuestionnaireContext==\"test-context\"");
        
        when(rulesByFlowRepository.findByFlow("test-flow")).thenReturn(Flux.just(rule));
        when(redisCacheService.getDataFromRedis(anyString(), anyString(), any(Map.class))).thenReturn(Mono.empty());
        when(rulesByFlowRepository.findByActionId(anyString())).thenReturn(Flux.empty());
        
        StepVerifier.create(iqeService.questionnaireByFlowAndCondition(rulesDetails, iqeOutput, headers))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
}
