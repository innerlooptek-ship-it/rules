package com.cvshealth.digital.microservice.iqe.controller;

import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
import com.cvshealth.digital.microservice.iqe.dto.RelatedQuestionsRequest;
import com.cvshealth.digital.microservice.iqe.exception.CvsException;
import com.cvshealth.digital.microservice.iqe.model.IQEResponse;
import com.cvshealth.digital.microservice.iqe.model.Questions;
import com.cvshealth.digital.microservice.iqe.model.RulesDetails;
import com.cvshealth.digital.microservice.iqe.service.IQEService;
import com.cvshealth.digital.microservice.iqe.testdata.TestDataBuilder;
import com.cvshealth.digital.microservice.iqe.utils.LoggingUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IQEControllerComprehensiveUnitTest {
    
    @Mock
    private IQEService iqeService;
    
    @Mock
    private LoggingUtils loggingUtils;
    
    @Mock
    private Map<String, String> errorMessages;
    
    @InjectMocks
    private IQEController iqeController;
    
    @Test
    void shouldGetQuestionnaireSuccessfully() {
        RulesDetails rulesDetails = TestDataBuilder.createTestRulesDetails();
        Map<String, String> headers = new HashMap<>();
        Questions expectedResponse = TestDataBuilder.createTestQuestionsModel();
        
        doNothing().when(loggingUtils).entryEventLogging(any(), any());
        doNothing().when(loggingUtils).exitEventLogging(any(), any());
        when(iqeService.getRuleDetails(any(), any(), any())).thenReturn(Mono.just(expectedResponse));
        
        StepVerifier.create(iqeController.getQuestionnaire(rulesDetails, headers))
                .expectNextMatches(result -> result != null && result.equals(expectedResponse))
                .verifyComplete();
    }
    
    @Test
    void shouldCreateQuestionnaireSuccessfully() {
        QuestionareRequest request = TestDataBuilder.createTestQuestionareRequest();
        Map<String, String> headers = new HashMap<>();
        Map<String, String> reqHdrMap = new HashMap<>();
        IQEResponse expectedResponse = new IQEResponse("0000", "Success", "test-action-id");
        
        doNothing().when(loggingUtils).entryEventLogging(any(), any());
        doNothing().when(loggingUtils).exitEventLogging(any(), any());
        when(iqeService.processQuestionnaire(any(), any(), any(), any())).thenReturn(Mono.just(expectedResponse));
        
        StepVerifier.create(iqeController.createQuestionnaire(request, headers, reqHdrMap))
                .expectNextMatches(result -> result != null && "0000".equals(result.getStatusCode()))
                .verifyComplete();
    }
    
    @Test
    void shouldGetAllRulesSuccessfully() {
        Map<String, String> headers = new HashMap<>();
        Map<String, String> reqHdrMap = new HashMap<>();
        QuestionareRequest expectedResponse = TestDataBuilder.createTestQuestionareRequest();
        
        doNothing().when(loggingUtils).entryEventLogging(any(), any());
        doNothing().when(loggingUtils).exitEventLogging(any(), any());
        when(iqeService.rules()).thenReturn(Mono.just(expectedResponse));
        
        StepVerifier.create(iqeController.getAllRules(headers, reqHdrMap))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldGetQuestionnaireByActionIdSuccessfully() {
        String actionId = "test-action-id";
        Map<String, String> headers = new HashMap<>();
        Map<String, String> reqHdrMap = new HashMap<>();
        QuestionareRequest expectedResponse = TestDataBuilder.createTestQuestionareRequest();
        
        doNothing().when(loggingUtils).entryEventLogging(any(), any());
        doNothing().when(loggingUtils).exitEventLogging(any(), any());
        when(iqeService.questionnaireByActionId(anyString(), any())).thenReturn(Mono.just(expectedResponse));
        
        StepVerifier.create(iqeController.questionnaireByActionId(actionId, headers, reqHdrMap))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldDeleteQuestionnaireByActionIdSuccessfully() {
        String actionId = "test-action-id";
        Map<String, String> headers = new HashMap<>();
        Map<String, String> reqHdrMap = new HashMap<>();
        IQEResponse expectedResponse = new IQEResponse("0000", "Deleted successfully", null);
        
        doNothing().when(loggingUtils).entryEventLogging(any(), any());
        doNothing().when(loggingUtils).exitEventLogging(any(), any());
        when(iqeService.deleteQuestionnaireByActionId(actionId)).thenReturn(Mono.just(expectedResponse));
        
        StepVerifier.create(iqeController.deleteQuestionnaireByActionId(actionId, headers, reqHdrMap))
                .expectNextMatches(result -> result != null && "0000".equals(result.getStatusCode()))
                .verifyComplete();
    }
    
    @Test
    void shouldGetQuestionnaireByFlowAndConditionSuccessfully() {
        RulesDetails rulesDetails = TestDataBuilder.createTestRulesDetails();
        Map<String, String> headers = new HashMap<>();
        QuestionareRequest expectedResponse = TestDataBuilder.createTestQuestionareRequest();
        
        doNothing().when(loggingUtils).entryEventLogging(any(), any());
        doNothing().when(loggingUtils).exitEventLogging(any(), any());
        when(iqeService.questionnaireByFlowAndCondition(any(), any(), any())).thenReturn(Mono.just(expectedResponse));
        
        StepVerifier.create(iqeController.questionnaireByFlowAndCondition(rulesDetails, headers))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldGetQuestionnaireByActionAndQuestionIdSuccessfully() {
        String actionId = "test-action-id";
        String questionId = "test-question-id";
        Map<String, String> headers = new HashMap<>();
        QuestionareRequest expectedResponse = TestDataBuilder.createTestQuestionareRequest();
        
        doNothing().when(loggingUtils).entryEventLogging(any(), any());
        doNothing().when(loggingUtils).exitEventLogging(any(), any());
        when(iqeService.questionnaireByActionAndQuestionId(actionId, questionId, headers)).thenReturn(Mono.just(expectedResponse));
        
        StepVerifier.create(iqeController.questionnaireByActionAndQuestionId(actionId, questionId, headers))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldGetQuestionnaireByFlowSuccessfully() {
        RulesDetails rulesDetails = TestDataBuilder.createTestRulesDetails();
        Map<String, String> headers = new HashMap<>();
        QuestionareRequest expectedResponse = TestDataBuilder.createTestQuestionareRequest();
        
        doNothing().when(loggingUtils).entryEventLogging(any(), any());
        doNothing().when(loggingUtils).exitEventLogging(any(), any());
        when(iqeService.questionnaireByFlow(any(), any(), any())).thenReturn(Mono.just(expectedResponse));
        
        StepVerifier.create(iqeController.questionnaireByFlow(rulesDetails, headers))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldGetQuestionsByActionAndQuestionIdSuccessfully() {
        RelatedQuestionsRequest relatedQuestions = new RelatedQuestionsRequest();
        relatedQuestions.setRelatedQuestions(List.of(TestDataBuilder.createTestQuestionDto()));
        Map<String, String> headers = new HashMap<>();
        QuestionareRequest expectedResponse = TestDataBuilder.createTestQuestionareRequest();
        
        doNothing().when(loggingUtils).entryEventLogging(any(), any());
        doNothing().when(loggingUtils).exitEventLogging(any(), any());
        when(iqeService.getQuestionsByRelatedQuestionsList(any(), any(), any())).thenReturn(Mono.just(expectedResponse));
        
        StepVerifier.create(iqeController.questionsByActionAndQuestionId(relatedQuestions, headers))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleServiceExceptionInGetQuestionnaire() {
        RulesDetails rulesDetails = TestDataBuilder.createTestRulesDetails();
        Map<String, String> headers = new HashMap<>();
        
        doNothing().when(loggingUtils).entryEventLogging(any(), any());
        doNothing().when(loggingUtils).exitEventLogging(any(), any());
        when(iqeService.getRuleDetails(any(), any(), any())).thenReturn(Mono.error(new RuntimeException("Service error")));
        
        StepVerifier.create(iqeController.getQuestionnaire(rulesDetails, headers))
                .expectError(RuntimeException.class)
                .verify();
    }
    
    @Test
    void shouldHandleCvsExceptionInCreateQuestionnaire() {
        QuestionareRequest request = TestDataBuilder.createTestQuestionareRequest();
        Map<String, String> headers = new HashMap<>();
        Map<String, String> reqHdrMap = new HashMap<>();
        
        doNothing().when(loggingUtils).entryEventLogging(any(), any());
        doNothing().when(loggingUtils).exitEventLogging(any(), any());
        CvsException cvsException = new CvsException(400, "BAD_REQUEST", "Invalid request", "Bad request", "Validation failed");
        when(iqeService.processQuestionnaire(any(), any(), any(), any())).thenReturn(Mono.error(cvsException));
        
        StepVerifier.create(iqeController.createQuestionnaire(request, headers, reqHdrMap))
                .expectError(CvsException.class)
                .verify();
    }
    
    @Test
    void shouldHandleEmptyRulesInGetAllRules() {
        Map<String, String> headers = new HashMap<>();
        Map<String, String> reqHdrMap = new HashMap<>();
        QuestionareRequest emptyResponse = new QuestionareRequest();
        emptyResponse.setActiveRules(List.of());
        emptyResponse.setInactiveRules(List.of());
        
        doNothing().when(loggingUtils).entryEventLogging(any(), any());
        doNothing().when(loggingUtils).exitEventLogging(any(), any());
        when(iqeService.rules()).thenReturn(Mono.just(emptyResponse));
        
        StepVerifier.create(iqeController.getAllRules(headers, reqHdrMap))
                .expectNextMatches(result -> 
                    result != null && 
                    "No Rules available".equals(result.getErrorDescription()))
                .verifyComplete();
    }
}
