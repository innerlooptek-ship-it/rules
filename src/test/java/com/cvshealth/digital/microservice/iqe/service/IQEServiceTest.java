package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
import com.cvshealth.digital.microservice.iqe.dto.Questions;
import com.cvshealth.digital.microservice.iqe.dto.RelatedQuestionsRequest;
import com.cvshealth.digital.microservice.iqe.model.IQEResponse;
import com.cvshealth.digital.microservice.iqe.entity.ActionsEntity;
import com.cvshealth.digital.microservice.iqe.entity.AnswerOptionsEntity;
import com.cvshealth.digital.microservice.iqe.entity.QuestionsEntity;
import com.cvshealth.digital.microservice.iqe.entity.RulesByFlowEntity;
import com.cvshealth.digital.microservice.iqe.model.RulesDetails;
import com.cvshealth.digital.microservice.iqe.repository.ActionsRepository;
import com.cvshealth.digital.microservice.iqe.repository.AnswerOptionsRepository;
import com.cvshealth.digital.microservice.iqe.repository.QuestionsRepository;
import com.cvshealth.digital.microservice.iqe.repository.RulesByFlowRepository;
import com.cvshealth.digital.microservice.iqe.repository.QuestionnaireRulesRepository;
import com.cvshealth.digital.microservice.iqe.repository.QuestionnaireDetailsRepository;
import com.cvshealth.digital.microservice.iqe.service.RedisCacheService;
import com.cvshealth.digital.microservice.iqe.service.RulesServiceRepoOrchestrator;
import com.cvshealth.digital.microservice.iqe.utils.LoggingUtils;
import com.cvshealth.digital.microservice.iqe.testdata.TestDataBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;
import java.util.HashMap;

@ExtendWith(MockitoExtension.class)
class IQEServiceTest {
    
    @Mock
    private ActionsRepository actionsRepository;
    @Mock
    private QuestionsRepository questionsRepository;
    @Mock
    private AnswerOptionsRepository answerOptionsRepository;
    @Mock
    private RulesByFlowRepository rulesByFlowRepository;
    @Mock
    private QuestionnaireRulesRepository questionnaireRulesRepository;
    @Mock
    private QuestionnaireDetailsRepository questionnaireDetailsRepository;
    @Mock
    private RedisCacheService redisCacheService;
    @Mock
    private RulesServiceRepoOrchestrator rulesServiceRepoOrchestrator;
    @Mock
    private LoggingUtils loggingUtils;
    @Mock
    private IQERepoOrchestrator iqeRepoOrchestrator;
    
    @InjectMocks
    private IQEService iqeService;
    
    @Test
    void shouldGetRuleDetailsSuccessfully() {
        RulesDetails rulesDetails = TestDataBuilder.createTestRulesDetails();
        
        when(questionnaireRulesRepository.findByFlow("test-flow"))
                .thenReturn(Flux.empty());
        
        StepVerifier.create(iqeService.getRuleDetails(rulesDetails, new HashMap<>(), new HashMap<>()))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleEmptyRulesGracefully() {
        RulesDetails rulesDetails = TestDataBuilder.createTestRulesDetails();
        
        when(questionnaireRulesRepository.findByFlow("test-flow"))
                .thenReturn(Flux.empty());
        
        StepVerifier.create(iqeService.getRuleDetails(rulesDetails, new HashMap<>(), new HashMap<>()))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldGetQuestionnaireByActionIdSuccessfully() {
        ActionsEntity action = TestDataBuilder.createTestAction();
        RulesByFlowEntity rule = TestDataBuilder.createTestRule();
        QuestionareRequest output = new QuestionareRequest();
        
        lenient().when(rulesByFlowRepository.findByActionId("test-action-id"))
                .thenReturn(Flux.just(rule));
        lenient().when(actionsRepository.findByActionId("test-action-id"))
                .thenReturn(Flux.just(action));
        lenient().when(iqeRepoOrchestrator.processQuestionnaire(any(), any(), any()))
                .thenReturn(Mono.just(new Questions()));
        
        StepVerifier.create(iqeService.questionnaireByActionId("test-action-id", output))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldGetQuestionnaireByActionAndQuestionIdSuccessfully() {
        QuestionsEntity question = TestDataBuilder.createTestQuestion();
        AnswerOptionsEntity option = TestDataBuilder.createTestAnswerOption();
        
        when(questionsRepository.findByActionIdAndQuestionId("test-action-id", "question-1"))
                .thenReturn(Mono.just(question));
        when(answerOptionsRepository.findByActionIdAndQuestionId("test-action-id", "question-1"))
                .thenReturn(Flux.just(option));
        
        StepVerifier.create(iqeService.questionnaireByActionAndQuestionId("test-action-id", "question-1", Map.of()))
                .expectNextMatches(result -> 
                    result != null && 
                    result.getQuestion() != null &&
                    result.getQuestion().getAnswerOptions() != null)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleQuestionnaireByFlowAndCondition() {
        RulesDetails rulesDetails = TestDataBuilder.createTestRulesDetails();
        RulesByFlowEntity rule = TestDataBuilder.createTestRule();
        QuestionareRequest output = new QuestionareRequest();
        
        when(rulesByFlowRepository.findByFlow("test-flow"))
                .thenReturn(Flux.just(rule));
        
        StepVerifier.create(iqeService.questionnaireByFlowAndCondition(rulesDetails, output, Map.of()))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleQuestionnaireByFlow() {
        RulesDetails rulesDetails = TestDataBuilder.createTestRulesDetails();
        RulesByFlowEntity rule = TestDataBuilder.createTestRule();
        ActionsEntity action = TestDataBuilder.createTestAction();
        QuestionareRequest output = new QuestionareRequest();
        
        when(rulesByFlowRepository.findByFlow("test-flow"))
                .thenReturn(Flux.just(rule));
        when(actionsRepository.findByActionId("test-action-id"))
                .thenReturn(Flux.just(action));
        when(questionsRepository.findByActionIdAndQuestionId(anyString(), anyString()))
                .thenReturn(Mono.just(TestDataBuilder.createTestQuestion()));
        when(answerOptionsRepository.findByActionIdAndQuestionId(anyString(), anyString()))
                .thenReturn(Flux.just(TestDataBuilder.createTestAnswerOption()));
        
        StepVerifier.create(iqeService.questionnaireByFlow(rulesDetails, output, Map.of()))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldGetQuestionsByRelatedQuestionsList() {
        RelatedQuestionsRequest request = TestDataBuilder.createTestRelatedQuestionsRequest();
        QuestionareRequest output = new QuestionareRequest();
        
        when(questionsRepository.findByActionIdAndQuestionId("test-action-id", "question-1"))
                .thenReturn(Mono.just(TestDataBuilder.createTestQuestion()));
        when(answerOptionsRepository.findByActionIdAndQuestionId("test-action-id", "question-1"))
                .thenReturn(Flux.just(TestDataBuilder.createTestAnswerOption()));
        
        StepVerifier.create(iqeService.getQuestionsByRelatedQuestionsList(request, output, Map.of()))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldDeleteQuestionnaireByActionId() {
        RulesByFlowEntity rule = TestDataBuilder.createTestRule();
        when(rulesByFlowRepository.findByActionId("test-action-id"))
                .thenReturn(Flux.just(rule));
        when(rulesByFlowRepository.deleteByFlowAndRuleId(anyString(), anyString()))
                .thenReturn(Mono.empty());
        when(actionsRepository.deleteByActionId("test-action-id"))
                .thenReturn(Mono.empty());
        when(questionsRepository.deleteByActionId("test-action-id"))
                .thenReturn(Mono.empty());
        when(answerOptionsRepository.deleteByActionId("test-action-id"))
                .thenReturn(Mono.empty());
        when(questionnaireDetailsRepository.deleteByActionId("test-action-id"))
                .thenReturn(Mono.empty());
        when(redisCacheService.deleteDataFromRedis(anyString(), anyString(), any()))
                .thenReturn(Mono.empty());
        
        StepVerifier.create(iqeService.deleteQuestionnaireByActionId("test-action-id"))
                .expectNextMatches(result -> 
                    result != null && 
                    "0000".equals(result.getStatusCode()) &&
                    "ActionId deleted successfully".equals(result.getStatusDescription()))
                .verifyComplete();
    }
}
