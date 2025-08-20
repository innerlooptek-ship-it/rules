package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
import com.cvshealth.digital.microservice.iqe.dto.RelatedQuestionsRequest;
import com.cvshealth.digital.microservice.iqe.entity.ActionsEntity;
import com.cvshealth.digital.microservice.iqe.entity.AnswerOptionsEntity;
import com.cvshealth.digital.microservice.iqe.entity.QuestionsEntity;
import com.cvshealth.digital.microservice.iqe.entity.RulesByFlowEntity;
import com.cvshealth.digital.microservice.iqe.entity.QuestionnaireRules;
import com.cvshealth.digital.microservice.iqe.model.IQEResponse;
import com.cvshealth.digital.microservice.iqe.model.Questions;
import com.cvshealth.digital.microservice.iqe.model.RulesDetails;
import com.cvshealth.digital.microservice.iqe.repository.ActionsRepository;
import com.cvshealth.digital.microservice.iqe.repository.AnswerOptionsRepository;
import com.cvshealth.digital.microservice.iqe.repository.QuestionsRepository;
import com.cvshealth.digital.microservice.iqe.repository.QuestionnaireDetailsRepository;
import com.cvshealth.digital.microservice.iqe.repository.QuestionnaireRulesRepository;
import com.cvshealth.digital.microservice.iqe.repository.RulesByFlowRepository;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IQEServiceComprehensiveTest {
    
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
        lenient().when(redisCacheService.getDataFromRedis(anyString(), anyString(), any(Map.class)))
                .thenReturn(Mono.empty());
        lenient().when(redisCacheService.setDataToRedisRest(any(), any(), any()))
                .thenReturn(Mono.empty());
        lenient().when(redisCacheService.deleteDataFromRedis(anyString(), anyString(), any(Map.class)))
                .thenReturn(Mono.empty());
    }
    
    @Test
    void shouldHandleNullRulesDetailsGracefully() {
        Map<String, String> headers = new HashMap<>();
        Map<String, Object> reqHeaders = new HashMap<>();
        
        StepVerifier.create(iqeService.getRuleDetails(null, headers, reqHeaders))
                .expectError(NullPointerException.class)
                .verify();
    }
    
    @Test
    void shouldHandleNullFlowInRulesDetails() {
        RulesDetails rulesDetails = new RulesDetails();
        rulesDetails.setFlow(null);
        Map<String, String> headers = new HashMap<>();
        Map<String, Object> reqHeaders = new HashMap<>();
        
        StepVerifier.create(iqeService.getRuleDetails(rulesDetails, headers, reqHeaders))
                .expectError(NullPointerException.class)
                .verify();
    }
    
    @Test
    void shouldHandleEmptyFlowInRulesDetails() {
        RulesDetails rulesDetails = new RulesDetails();
        rulesDetails.setFlow("");
        Map<String, String> headers = new HashMap<>();
        Map<String, Object> reqHeaders = new HashMap<>();
        
        when(rulesRepository.findByFlow("")).thenReturn(Flux.empty());
        
        StepVerifier.create(iqeService.getRuleDetails(rulesDetails, headers, reqHeaders))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleRepositoryErrorInGetRuleDetails() {
        RulesDetails rulesDetails = TestDataBuilder.createTestRulesDetails();
        Map<String, String> headers = new HashMap<>();
        Map<String, Object> reqHeaders = new HashMap<>();
        
        when(rulesRepository.findByFlow(anyString()))
                .thenReturn(Flux.error(new RuntimeException("Database error")));
        
        StepVerifier.create(iqeService.getRuleDetails(rulesDetails, headers, reqHeaders))
                .expectError(RuntimeException.class)
                .verify();
    }
    
    @Test
    void shouldHandleRepositoryErrorInQuestionnaireByFlow() {
        RulesDetails rulesDetails = TestDataBuilder.createTestRulesDetails();
        QuestionareRequest iqeOutput = new QuestionareRequest();
        Map<String, String> headers = new HashMap<>();
        
        when(rulesByFlowRepository.findByFlow(anyString()))
                .thenReturn(Flux.error(new RuntimeException("Database error")));
        
        StepVerifier.create(iqeService.questionnaireByFlow(rulesDetails, iqeOutput, headers))
                .expectError(RuntimeException.class)
                .verify();
    }
    
    @Test
    void shouldHandleRepositoryErrorInQuestionnaireByFlowAndCondition() {
        RulesDetails rulesDetails = TestDataBuilder.createTestRulesDetails();
        QuestionareRequest iqeOutput = new QuestionareRequest();
        Map<String, String> headers = new HashMap<>();
        
        when(rulesByFlowRepository.findByFlow(anyString()))
                .thenReturn(Flux.error(new RuntimeException("Database error")));
        
        StepVerifier.create(iqeService.questionnaireByFlowAndCondition(rulesDetails, iqeOutput, headers))
                .expectError(RuntimeException.class)
                .verify();
    }
    
    @Test
    void shouldHandleRepositoryErrorInQuestionnaireByActionId() {
        String actionId = "test-action-id";
        QuestionareRequest iqeOutput = new QuestionareRequest();
        
        when(rulesByFlowRepository.findByActionId(anyString()))
                .thenReturn(Flux.error(new RuntimeException("Database error")));
        
        StepVerifier.create(iqeService.questionnaireByActionId(actionId, iqeOutput))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleRepositoryErrorInQuestionnaireByActionAndQuestionId() {
        String actionId = "test-action-id";
        String questionId = "test-question-id";
        Map<String, String> headers = new HashMap<>();
        
        when(questionsRepository.findByActionIdAndQuestionId(anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Database error")));
        
        StepVerifier.create(iqeService.questionnaireByActionAndQuestionId(actionId, questionId, headers))
                .expectError(RuntimeException.class)
                .verify();
    }
    
    @Test
    void shouldHandleRepositoryErrorInGetQuestionsByRelatedQuestionsList() {
        RelatedQuestionsRequest relatedQuestions = new RelatedQuestionsRequest();
        com.cvshealth.digital.microservice.iqe.dto.Questions question = TestDataBuilder.createTestQuestionDto();
        relatedQuestions.setRelatedQuestions(List.of(question));
        
        QuestionareRequest iqeOutput = new QuestionareRequest();
        Map<String, String> headers = new HashMap<>();
        
        when(questionsRepository.findByActionIdAndQuestionId(anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Database error")));
        
        StepVerifier.create(iqeService.getQuestionsByRelatedQuestionsList(relatedQuestions, iqeOutput, headers))
                .expectError(RuntimeException.class)
                .verify();
    }
    
    @Test
    void shouldHandleRepositoryErrorInRules() {
        when(rulesByFlowRepository.findAll())
                .thenReturn(Flux.error(new RuntimeException("Database error")));
        
        StepVerifier.create(iqeService.rules())
                .expectError(RuntimeException.class)
                .verify();
    }
    
    @Test
    void shouldHandleProcessQuestionnaireWithNullRequest() {
        IQEResponse iqeResponse = new IQEResponse();
        Map<String, String> reqHdrMap = new HashMap<>();
        Map<String, Object> eventMap = new HashMap<>();
        
        StepVerifier.create(iqeService.processQuestionnaire(null, iqeResponse, reqHdrMap, eventMap))
                .expectError()
                .verify();
    }
    
    @Test
    void shouldHandleProcessQuestionnaireWithRepositoryError() {
        QuestionareRequest request = TestDataBuilder.createTestQuestionareRequest();
        IQEResponse iqeResponse = new IQEResponse();
        Map<String, String> reqHdrMap = new HashMap<>();
        Map<String, Object> eventMap = new HashMap<>();
        
        when(rulesServiceRepoOrchestrator.validateRequest(any(), any()))
                .thenReturn(Mono.error(new RuntimeException("Processing error")));
        
        StepVerifier.create(iqeService.processQuestionnaire(request, iqeResponse, reqHdrMap, eventMap))
                .expectError(RuntimeException.class)
                .verify();
    }
    
    @Test
    void shouldHandleQuestionnaireByFlowWithEmptyRules() {
        RulesDetails rulesDetails = TestDataBuilder.createTestRulesDetails();
        QuestionareRequest iqeOutput = new QuestionareRequest();
        Map<String, String> headers = new HashMap<>();
        
        when(rulesByFlowRepository.findByFlow(anyString()))
                .thenReturn(Flux.empty());
        
        StepVerifier.create(iqeService.questionnaireByFlow(rulesDetails, iqeOutput, headers))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleQuestionnaireByFlowAndConditionWithEmptyRules() {
        RulesDetails rulesDetails = TestDataBuilder.createTestRulesDetails();
        QuestionareRequest iqeOutput = new QuestionareRequest();
        Map<String, String> headers = new HashMap<>();
        
        when(rulesByFlowRepository.findByFlow(anyString()))
                .thenReturn(Flux.empty());
        
        StepVerifier.create(iqeService.questionnaireByFlowAndCondition(rulesDetails, iqeOutput, headers))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleQuestionnaireByActionIdWithEmptyRules() {
        String actionId = "test-action-id";
        QuestionareRequest iqeOutput = new QuestionareRequest();
        
        when(rulesByFlowRepository.findByActionId(anyString()))
                .thenReturn(Flux.empty());
        
        StepVerifier.create(iqeService.questionnaireByActionId(actionId, iqeOutput))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleQuestionnaireByActionAndQuestionIdWithEmptyQuestion() {
        String actionId = "test-action-id";
        String questionId = "test-question-id";
        Map<String, String> headers = new HashMap<>();
        
        when(questionsRepository.findByActionIdAndQuestionId(anyString(), anyString()))
                .thenReturn(Mono.empty());
        
        StepVerifier.create(iqeService.questionnaireByActionAndQuestionId(actionId, questionId, headers))
                .expectError()
                .verify();
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
    void shouldHandleProcessQuestionnaireSuccessfully() {
        QuestionareRequest request = TestDataBuilder.createTestQuestionareRequest();
        IQEResponse iqeResponse = new IQEResponse();
        iqeResponse.setStatusCode("0000");
        Map<String, String> reqHdrMap = new HashMap<>();
        Map<String, Object> eventMap = new HashMap<>();
        
        when(rulesServiceRepoOrchestrator.validateRequest(any(), any()))
                .thenReturn(Mono.just(request));
        when(iqeRepoOrchestrator.assignSequenceIds(any()))
                .thenReturn(Mono.just(request));
        when(iqeRepoOrchestrator.processInputData(any(), any(), any(), any()))
                .thenReturn(Mono.just(request));
        when(iqeRepoOrchestrator.insertQuestionsIntoDB(any(), any(), any()))
                .thenReturn(Mono.empty());
        
        StepVerifier.create(iqeService.processQuestionnaire(request, iqeResponse, reqHdrMap, eventMap))
                .expectNextMatches(result -> result != null && "0000".equals(result.getStatusCode()))
                .verifyComplete();
    }
    
    @Test
    void shouldHandleRulesWithMultipleActiveAndInactiveRules() {
        RulesByFlowEntity activeRule1 = TestDataBuilder.createTestRule();
        activeRule1.setActive(true);
        activeRule1.setRuleId("active-rule-1");
        
        RulesByFlowEntity activeRule2 = TestDataBuilder.createTestRule();
        activeRule2.setActive(true);
        activeRule2.setRuleId("active-rule-2");
        
        RulesByFlowEntity inactiveRule1 = TestDataBuilder.createTestRule();
        inactiveRule1.setActive(false);
        inactiveRule1.setRuleId("inactive-rule-1");
        
        RulesByFlowEntity inactiveRule2 = TestDataBuilder.createTestRule();
        inactiveRule2.setActive(false);
        inactiveRule2.setRuleId("inactive-rule-2");
        
        when(rulesByFlowRepository.findAll())
                .thenReturn(Flux.just(activeRule1, activeRule2, inactiveRule1, inactiveRule2));
        
        StepVerifier.create(iqeService.rules())
                .expectNextMatches(result -> 
                    result != null && 
                    result.getActiveRules() != null && 
                    result.getInactiveRules() != null &&
                    result.getActiveRules().size() == 2 &&
                    result.getInactiveRules().size() == 2)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleQuestionnaireByFlowWithMultipleQuestions() {
        RulesDetails rulesDetails = TestDataBuilder.createTestRulesDetails();
        rulesDetails.setFlow("test-flow");
        rulesDetails.setActionId("test-action-id");
        
        QuestionareRequest iqeOutput = new QuestionareRequest();
        Map<String, String> headers = new HashMap<>();
        
        RulesByFlowEntity rule = TestDataBuilder.createTestRule();
        ActionsEntity action = TestDataBuilder.createTestAction();
        action.setQuestionId(List.of("question-1", "question-2"));
        
        QuestionsEntity question1 = TestDataBuilder.createTestQuestionWithIds("test-action-id", "question-1");
        QuestionsEntity question2 = TestDataBuilder.createTestQuestionWithIds("test-action-id", "question-2");
        
        AnswerOptionsEntity option1 = TestDataBuilder.createTestAnswerOptionWithIds("test-action-id", "question-1", "option-1");
        AnswerOptionsEntity option2 = TestDataBuilder.createTestAnswerOptionWithIds("test-action-id", "question-2", "option-2");
        
        when(rulesByFlowRepository.findByFlow("test-flow")).thenReturn(Flux.just(rule));
        when(actionsRepository.findByActionId(anyString())).thenReturn(Flux.just(action));
        
        when(questionsRepository.findByActionIdAndQuestionId("test-action-id", "question-1"))
                .thenReturn(Mono.just(question1));
        when(questionsRepository.findByActionIdAndQuestionId("test-action-id", "question-2"))
                .thenReturn(Mono.just(question2));
        
        when(answerOptionsRepository.findByActionIdAndQuestionId("test-action-id", "question-1"))
                .thenReturn(Flux.just(option1));
        when(answerOptionsRepository.findByActionIdAndQuestionId("test-action-id", "question-2"))
                .thenReturn(Flux.just(option2));
        
        StepVerifier.create(iqeService.questionnaireByFlow(rulesDetails, iqeOutput, headers))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
}
