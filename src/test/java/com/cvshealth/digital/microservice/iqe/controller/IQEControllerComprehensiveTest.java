package com.cvshealth.digital.microservice.iqe.controller;

import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
import com.cvshealth.digital.microservice.iqe.model.IQEResponse;
import com.cvshealth.digital.microservice.iqe.model.Questions;
import com.cvshealth.digital.microservice.iqe.model.RulesDetails;
import com.cvshealth.digital.microservice.iqe.service.IQEService;
import com.cvshealth.digital.microservice.iqe.testdata.TestDataBuilder;
import com.cvshealth.digital.microservice.iqe.utils.LoggingUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IQEControllerComprehensiveTest {
    
    private WebTestClient webTestClient;
    
    @Mock
    private IQEService iqeService;
    
    @Mock
    private LoggingUtils loggingUtils;
    
    @Mock
    private Map<String, String> errorMessages;
    
    @InjectMocks
    private IQEController iqeController;
    
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(iqeController).build();
        objectMapper = new ObjectMapper();
    }
    
    @Test
    void shouldGetQuestionnaireRulesSuccessfully() throws Exception {
        RulesDetails request = TestDataBuilder.createTestRulesDetails();
        Questions response = new Questions();
        
        when(iqeService.getRuleDetails(any(RulesDetails.class), any(Map.class), any(Map.class)))
                .thenReturn(Mono.just(response));
        
        webTestClient.post()
                .uri("/schedule/iqe/v1/getquestionnairerules")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }
    
    @Test
    void shouldHandleDynamicFlowConditionEvaluation() throws Exception {
        RulesDetails request = TestDataBuilder.createTestRulesDetails();
        QuestionareRequest response = new QuestionareRequest();
        
        when(iqeService.questionnaireByFlowAndCondition(any(RulesDetails.class), any(QuestionareRequest.class), any(Map.class)))
                .thenReturn(Mono.just(response));
        
        webTestClient.post()
                .uri("/schedule/iqe/v1/questionnaires/dynamic-flow-condition-evaluation")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }
    
    @Test
    void shouldGetQuestionnaireByActionId() throws Exception {
        QuestionareRequest response = new QuestionareRequest();
        
        when(iqeService.questionnaireByActionId(any(String.class), any(QuestionareRequest.class)))
                .thenReturn(Mono.just(response));
        
        webTestClient.get()
                .uri("/schedule/iqe/v1/questionnaire/{actionId}", "test-action-id")
                .exchange()
                .expectStatus().isOk();
    }
    
    @Test
    void shouldDeleteQuestionnaireByActionId() throws Exception {
        IQEResponse response = new IQEResponse();
        response.setStatusCode("0000");
        
        when(iqeService.deleteQuestionnaireByActionId(any(String.class)))
                .thenReturn(Mono.just(response));
        
        webTestClient.delete()
                .uri("/schedule/iqe/v1/questionnaire/{actionId}", "test-action-id")
                .exchange()
                .expectStatus().isOk();
    }
    
    @Test
    void shouldGetAllRules() throws Exception {
        QuestionareRequest response = new QuestionareRequest();
        response.setActiveRules(new java.util.ArrayList<>());
        response.setInactiveRules(new java.util.ArrayList<>());
        
        when(iqeService.rules())
                .thenReturn(Mono.just(response));
        
        webTestClient.get()
                .uri("/schedule/iqe/v1/rules")
                .exchange()
                .expectStatus().isOk();
    }
    
    @Test
    void shouldHandleErrorInGetQuestionnaireRules() throws Exception {
        RulesDetails request = TestDataBuilder.createTestRulesDetails();
        
        when(iqeService.getRuleDetails(any(RulesDetails.class), any(Map.class), any(Map.class)))
                .thenReturn(Mono.error(new RuntimeException("Service error")));
        
        webTestClient.post()
                .uri("/schedule/iqe/v1/getquestionnairerules")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError();
    }
    
    @Test
    void shouldHandleErrorInDynamicFlowConditionEvaluation() throws Exception {
        RulesDetails request = TestDataBuilder.createTestRulesDetails();
        
        when(iqeService.questionnaireByFlowAndCondition(any(RulesDetails.class), any(QuestionareRequest.class), any(Map.class)))
                .thenReturn(Mono.error(new RuntimeException("Service error")));
        
        webTestClient.post()
                .uri("/schedule/iqe/v1/questionnaires/dynamic-flow-condition-evaluation")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
