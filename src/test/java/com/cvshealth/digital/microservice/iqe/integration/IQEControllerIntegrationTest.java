package com.cvshealth.digital.microservice.iqe.integration;

import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
import com.cvshealth.digital.microservice.iqe.entity.ActionsEntity;
import com.cvshealth.digital.microservice.iqe.entity.QuestionsEntity;
import com.cvshealth.digital.microservice.iqe.entity.RulesByFlowEntity;
import com.cvshealth.digital.microservice.iqe.entity.AnswerOptionsEntity;
import com.cvshealth.digital.microservice.iqe.model.RulesDetails;
import com.cvshealth.digital.microservice.iqe.repository.ActionsRepository;
import com.cvshealth.digital.microservice.iqe.repository.QuestionsRepository;
import com.cvshealth.digital.microservice.iqe.repository.RulesByFlowRepository;
import com.cvshealth.digital.microservice.iqe.repository.AnswerOptionsRepository;
import com.cvshealth.digital.microservice.iqe.testdata.TestDataBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
@ActiveProfiles("test")
class IQEControllerIntegrationTest {
    
    @Container
    static CassandraContainer<?> cassandraContainer = new CassandraContainer<>("cassandra:4.1")
            .withExposedPorts(9042)
            .withStartupTimeout(Duration.ofMinutes(3));
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cassandra.contact-points", cassandraContainer::getHost);
        registry.add("spring.cassandra.port", cassandraContainer::getFirstMappedPort);
        registry.add("spring.cassandra.local-datacenter", () -> "datacenter1");
        registry.add("spring.cassandra.keyspace-name", () -> "iqe_test");
        registry.add("spring.cassandra.schema-action", () -> "create_if_not_exists");
    }
    
    @Autowired
    private WebTestClient webTestClient;
    
    @Autowired
    private ActionsRepository actionsRepository;
    
    @Autowired
    private QuestionsRepository questionsRepository;
    
    @Autowired
    private RulesByFlowRepository rulesByFlowRepository;
    
    @Autowired
    private AnswerOptionsRepository answerOptionsRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        actionsRepository.deleteAll().block();
        questionsRepository.deleteAll().block();
        rulesByFlowRepository.deleteAll().block();
        answerOptionsRepository.deleteAll().block();
    }
    
    @Test
    void shouldGetQuestionnaireRulesSuccessfully() throws Exception {
        RulesByFlowEntity rule = TestDataBuilder.createTestRule();
        ActionsEntity action = TestDataBuilder.createTestAction();
        QuestionsEntity question = TestDataBuilder.createTestQuestion();
        AnswerOptionsEntity option = TestDataBuilder.createTestAnswerOption();
        
        rulesByFlowRepository.save(rule).block();
        actionsRepository.save(action).block();
        questionsRepository.save(question).block();
        answerOptionsRepository.save(option).block();
        
        RulesDetails request = TestDataBuilder.createTestRulesDetails();
        
        webTestClient.post()
                .uri("/schedule/iqe/v1/getquestionnairerules")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }
    
    @Test
    void shouldHandleDynamicFlowConditionEvaluation() throws Exception {
        RulesByFlowEntity rule = TestDataBuilder.createTestRule();
        ActionsEntity action = TestDataBuilder.createTestAction();
        QuestionsEntity question = TestDataBuilder.createTestQuestion();
        AnswerOptionsEntity option = TestDataBuilder.createTestAnswerOption();
        
        rulesByFlowRepository.save(rule).block();
        actionsRepository.save(action).block();
        questionsRepository.save(question).block();
        answerOptionsRepository.save(option).block();
        
        RulesDetails request = TestDataBuilder.createTestRulesDetails();
        
        webTestClient.post()
                .uri("/schedule/iqe/v1/questionnaires/dynamic-flow-condition-evaluation")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }
    
    @Test
    void shouldGetQuestionnaireByActionId() {
        ActionsEntity action = TestDataBuilder.createTestAction();
        QuestionsEntity question = TestDataBuilder.createTestQuestion();
        AnswerOptionsEntity option = TestDataBuilder.createTestAnswerOption();
        RulesByFlowEntity rule = TestDataBuilder.createTestRule();
        
        actionsRepository.save(action).block();
        questionsRepository.save(question).block();
        answerOptionsRepository.save(option).block();
        rulesByFlowRepository.save(rule).block();
        
        webTestClient.get()
                .uri("/schedule/iqe/v1/questionnaire/{actionId}", "test-action-id")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }
    
    @Test
    void shouldGetAllRules() {
        RulesByFlowEntity rule = TestDataBuilder.createTestRule();
        rulesByFlowRepository.save(rule).block();
        
        webTestClient.get()
                .uri("/schedule/iqe/v1/rules")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }
    
    @Test
    void shouldDeleteQuestionnaireByActionId() {
        ActionsEntity action = TestDataBuilder.createTestAction();
        QuestionsEntity question = TestDataBuilder.createTestQuestion();
        AnswerOptionsEntity option = TestDataBuilder.createTestAnswerOption();
        RulesByFlowEntity rule = TestDataBuilder.createTestRule();
        
        actionsRepository.save(action).block();
        questionsRepository.save(question).block();
        answerOptionsRepository.save(option).block();
        rulesByFlowRepository.save(rule).block();
        
        webTestClient.delete()
                .uri("/schedule/iqe/v1/questionnaire/{actionId}", "test-action-id")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }
}
