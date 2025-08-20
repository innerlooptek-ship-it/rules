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
import com.cvshealth.digital.microservice.iqe.service.IQEService;
import com.cvshealth.digital.microservice.iqe.testdata.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class IQEServiceIntegrationTest {
    
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
    private IQEService iqeService;
    
    @Autowired
    private ActionsRepository actionsRepository;
    
    @Autowired
    private QuestionsRepository questionsRepository;
    
    @Autowired
    private RulesByFlowRepository rulesByFlowRepository;
    
    @Autowired
    private AnswerOptionsRepository answerOptionsRepository;
    
    @BeforeEach
    void setUp() {
        actionsRepository.deleteAll().block();
        questionsRepository.deleteAll().block();
        rulesByFlowRepository.deleteAll().block();
        answerOptionsRepository.deleteAll().block();
    }
    
    @Test
    void shouldGetQuestionnaireByActionIdWithRealData() {
        ActionsEntity action = TestDataBuilder.createTestAction();
        QuestionsEntity question = TestDataBuilder.createTestQuestion();
        AnswerOptionsEntity option = TestDataBuilder.createTestAnswerOption();
        RulesByFlowEntity rule = TestDataBuilder.createTestRule();
        
        actionsRepository.save(action).block();
        questionsRepository.save(question).block();
        answerOptionsRepository.save(option).block();
        rulesByFlowRepository.save(rule).block();
        
        QuestionareRequest output = new QuestionareRequest();
        
        StepVerifier.create(iqeService.questionnaireByActionId("test-action-id", output))
                .expectNextMatches(result -> 
                    result != null && 
                    result.getQuestions() != null)
                .verifyComplete();
    }
    
    @Test
    void shouldGetRuleDetailsWithRealData() {
        RulesByFlowEntity rule = TestDataBuilder.createTestRule();
        ActionsEntity action = TestDataBuilder.createTestAction();
        QuestionsEntity question = TestDataBuilder.createTestQuestion();
        AnswerOptionsEntity option = TestDataBuilder.createTestAnswerOption();
        
        rulesByFlowRepository.save(rule).block();
        actionsRepository.save(action).block();
        questionsRepository.save(question).block();
        answerOptionsRepository.save(option).block();
        
        RulesDetails rulesDetails = TestDataBuilder.createTestRulesDetails();
        Map<String, String> headers = new HashMap<>();
        Map<String, Object> reqHeaders = new HashMap<>();
        
        StepVerifier.create(iqeService.getRuleDetails(rulesDetails, headers, reqHeaders))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldDeleteQuestionnaireByActionIdWithRealData() {
        ActionsEntity action = TestDataBuilder.createTestAction();
        QuestionsEntity question = TestDataBuilder.createTestQuestion();
        AnswerOptionsEntity option = TestDataBuilder.createTestAnswerOption();
        RulesByFlowEntity rule = TestDataBuilder.createTestRule();
        
        actionsRepository.save(action).block();
        questionsRepository.save(question).block();
        answerOptionsRepository.save(option).block();
        rulesByFlowRepository.save(rule).block();
        
        StepVerifier.create(iqeService.deleteQuestionnaireByActionId("test-action-id"))
                .expectNextMatches(result -> 
                    result != null && 
                    "0000".equals(result.getStatusCode()))
                .verifyComplete();
        
        StepVerifier.create(actionsRepository.findByActionId("test-action-id"))
                .verifyComplete();
    }
    
    @Test
    void shouldHandleQuestionnaireByFlowAndConditionWithRealData() {
        RulesByFlowEntity rule = TestDataBuilder.createTestRule();
        ActionsEntity action = TestDataBuilder.createTestAction();
        QuestionsEntity question = TestDataBuilder.createTestQuestion();
        AnswerOptionsEntity option = TestDataBuilder.createTestAnswerOption();
        
        rulesByFlowRepository.save(rule).block();
        actionsRepository.save(action).block();
        questionsRepository.save(question).block();
        answerOptionsRepository.save(option).block();
        
        RulesDetails rulesDetails = TestDataBuilder.createTestRulesDetails();
        QuestionareRequest output = new QuestionareRequest();
        Map<String, String> headers = new HashMap<>();
        
        StepVerifier.create(iqeService.questionnaireByFlowAndCondition(rulesDetails, output, headers))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
}
