package com.cvshealth.digital.microservice.iqe.controller;

import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
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

import java.util.Map;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IQEControllerTest {
    
    @Mock
    private IQEService iqeService;
    
    @Mock
    private LoggingUtils loggingUtils;
    
    @Mock
    private Map<String, String> errorMessages;
    
    @InjectMocks
    private IQEController iqeController;
    
    @Test
    void shouldCallIQEServiceForGetQuestionnaire() {
        RulesDetails request = TestDataBuilder.createTestRulesDetails();
        com.cvshealth.digital.microservice.iqe.model.Questions response = new com.cvshealth.digital.microservice.iqe.model.Questions();
        Map<String, String> headers = new HashMap<>();
        
        when(iqeService.getRuleDetails(any(RulesDetails.class), any(Map.class), any(Map.class)))
                .thenReturn(Mono.just(response));
        
        Mono<com.cvshealth.digital.microservice.iqe.model.Questions> result = iqeController.getQuestionnaire(request, headers);
        
        StepVerifier.create(result)
                .expectNext(response)
                .verifyComplete();
        
        verify(iqeService).getRuleDetails(any(RulesDetails.class), any(Map.class), any(Map.class));
    }
    
    @Test
    void shouldCallIQEServiceForDynamicFlowConditionEvaluation() {
        RulesDetails request = TestDataBuilder.createTestRulesDetails();
        QuestionareRequest response = new QuestionareRequest();
        Map<String, String> headers = new HashMap<>();
        
        when(iqeService.questionnaireByFlowAndCondition(any(RulesDetails.class), any(QuestionareRequest.class), any(Map.class)))
                .thenReturn(Mono.just(response));
        
        Mono<QuestionareRequest> result = iqeController.questionnaireByFlowAndCondition(request, headers);
        
        StepVerifier.create(result)
                .expectNext(response)
                .verifyComplete();
        
        verify(iqeService).questionnaireByFlowAndCondition(any(RulesDetails.class), any(QuestionareRequest.class), any(Map.class));
    }
    
    @Test
    void shouldCallIQEServiceForQuestionnaireByActionId() {
        QuestionareRequest response = new QuestionareRequest();
        Map<String, String> headers = new HashMap<>();
        Map<String, String> reqHdrMap = new HashMap<>();
        
        when(iqeService.questionnaireByActionId(anyString(), any(QuestionareRequest.class)))
                .thenReturn(Mono.just(response));
        
        Mono<QuestionareRequest> result = iqeController.questionnaireByActionId("test-action-id", headers, reqHdrMap);
        
        StepVerifier.create(result)
                .expectNext(response)
                .verifyComplete();
        
        verify(iqeService).questionnaireByActionId(anyString(), any(QuestionareRequest.class));
    }
    
    @Test
    void shouldCallIQEServiceForGetAllRules() {
        QuestionareRequest response = new QuestionareRequest();
        response.setActiveRules(java.util.List.of(TestDataBuilder.createTestRule()));
        response.setInactiveRules(java.util.List.of());
        Map<String, String> headers = new HashMap<>();
        Map<String, String> reqHdrMap = new HashMap<>();
        
        when(iqeService.rules())
                .thenReturn(Mono.just(response));
        
        Mono<QuestionareRequest> result = iqeController.getAllRules(headers, reqHdrMap);
        
        StepVerifier.create(result)
                .expectNextMatches(res -> 
                    res != null && 
                    res.getActiveRules() != null && 
                    !res.getActiveRules().isEmpty())
                .verifyComplete();
        
        verify(iqeService).rules();
    }
}
