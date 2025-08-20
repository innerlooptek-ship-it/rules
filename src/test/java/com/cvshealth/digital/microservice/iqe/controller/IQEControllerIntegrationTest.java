package com.cvshealth.digital.microservice.iqe.controller;

import com.cvshealth.digital.microservice.iqe.config.BaseIntegrationTest;
import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
import com.cvshealth.digital.microservice.iqe.model.IQEResponse;
import com.cvshealth.digital.microservice.iqe.model.Questions;
import com.cvshealth.digital.microservice.iqe.model.RulesDetails;
import com.cvshealth.digital.microservice.iqe.repository.ActionsRepository;
import com.cvshealth.digital.microservice.iqe.repository.AnswerOptionsRepository;
import com.cvshealth.digital.microservice.iqe.repository.QuestionsRepository;
import com.cvshealth.digital.microservice.iqe.repository.RulesByFlowRepository;
import com.cvshealth.digital.microservice.iqe.testdata.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@AutoConfigureWebTestClient
class IQEControllerIntegrationTest extends BaseIntegrationTest {
    
    @Autowired
    private WebTestClient webTestClient;
    
    @Autowired
    private ActionsRepository actionsRepository;
    
    @Autowired
    private QuestionsRepository questionsRepository;
    
    @Autowired
    private AnswerOptionsRepository answerOptionsRepository;
    
    @Autowired
    private RulesByFlowRepository rulesByFlowRepository;
    
    @BeforeEach
    void setUp() {
        actionsRepository.deleteAll().block();
        questionsRepository.deleteAll().block();
        answerOptionsRepository.deleteAll().block();
        rulesByFlowRepository.deleteAll().block();
        
        rulesByFlowRepository.save(TestDataBuilder.createTestRule()).block();
        actionsRepository.save(TestDataBuilder.createTestAction()).block();
        questionsRepository.save(TestDataBuilder.createTestQuestion()).block();
        answerOptionsRepository.save(TestDataBuilder.createTestAnswerOption()).block();
    }
    
    @Test
    void shouldGetQuestionnaireRulesSuccessfully() {
        RulesDetails request = TestDataBuilder.createTestRulesDetails();
        
        webTestClient.post()
                .uri("/schedule/iqe/v1/getquestionnairerules")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), RulesDetails.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Questions.class);
    }
    
    @Test
    void shouldHandleDynamicFlowConditionEvaluation() {
        RulesDetails request = TestDataBuilder.createTestRulesDetails();
        
        webTestClient.post()
                .uri("/schedule/iqe/v1/questionnaires/dynamic-flow-condition-evaluation")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), RulesDetails.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(QuestionareRequest.class);
    }
    
    @Test
    void shouldCreateQuestionnaireSuccessfully() {
        QuestionareRequest request = TestDataBuilder.createTestQuestionareRequest();
        
        webTestClient.post()
                .uri("/schedule/iqe/v1/questionnaire")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), QuestionareRequest.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(IQEResponse.class)
                .value(response -> {
                    assert "0000".equals(response.getStatusCode());
                });
    }
    
    @Test
    void shouldGetQuestionnaireByActionId() {
        String actionId = "test-action-id";
        
        webTestClient.get()
                .uri("/schedule/iqe/v1/questionnaire/{actionId}", actionId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(QuestionareRequest.class);
    }
    
    @Test
    void shouldDeleteQuestionnaireByActionId() {
        String actionId = "test-action-id";
        
        webTestClient.delete()
                .uri("/schedule/iqe/v1/questionnaire/{actionId}", actionId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(IQEResponse.class)
                .value(response -> {
                    assert "0000".equals(response.getStatusCode());
                });
    }
    
    @Test
    void shouldGetAllRules() {
        webTestClient.get()
                .uri("/schedule/iqe/v1/rules")
                .exchange()
                .expectStatus().isOk()
                .expectBody(QuestionareRequest.class);
    }
    
    @Test
    void shouldHandleInvalidRequestForGetQuestionnaireRules() {
        RulesDetails request = new RulesDetails(); // Empty request
        
        webTestClient.post()
                .uri("/schedule/iqe/v1/getquestionnairerules")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), RulesDetails.class)
                .exchange()
                .expectStatus().is5xxServerError();
    }
    
    @Test
    void shouldHandleNonExistentActionId() {
        String nonExistentActionId = UUID.randomUUID().toString();
        
        webTestClient.get()
                .uri("/schedule/iqe/v1/questionnaire/{actionId}", nonExistentActionId)
                .exchange()
                .expectStatus().isOk() // Returns empty response, not an error
                .expectBody(QuestionareRequest.class);
    }
}
