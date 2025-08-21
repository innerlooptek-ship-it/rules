package com.cvshealth.digital.microservice.iqe.integration;

import com.cvshealth.digital.microservice.iqe.entity.ActionsEntity;
import com.cvshealth.digital.microservice.iqe.entity.QuestionsEntity;
import com.cvshealth.digital.microservice.iqe.entity.RulesByFlowEntity;
import com.cvshealth.digital.microservice.iqe.entity.AnswerOptionsEntity;
import com.cvshealth.digital.microservice.iqe.repository.ActionsRepository;
import com.cvshealth.digital.microservice.iqe.repository.QuestionsRepository;
import com.cvshealth.digital.microservice.iqe.repository.RulesByFlowRepository;
import com.cvshealth.digital.microservice.iqe.repository.AnswerOptionsRepository;
import com.cvshealth.digital.microservice.iqe.testdata.TestDataBuilder;
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

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class CassandraIntegrationTest {
    
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
    private ActionsRepository actionsRepository;
    
    @Autowired
    private QuestionsRepository questionsRepository;
    
    @Autowired
    private RulesByFlowRepository rulesByFlowRepository;
    
    @Autowired
    private AnswerOptionsRepository answerOptionsRepository;
    
    @Test
    void shouldSaveAndRetrieveAction() {
        ActionsEntity action = TestDataBuilder.createTestAction();
        
        StepVerifier.create(actionsRepository.save(action))
                .expectNext(action)
                .verifyComplete();
        
        StepVerifier.create(actionsRepository.findByActionId("test-action-id"))
                .expectNext(action)
                .verifyComplete();
    }
    
    @Test
    void shouldSaveAndRetrieveQuestion() {
        QuestionsEntity question = TestDataBuilder.createTestQuestion();
        
        StepVerifier.create(questionsRepository.save(question))
                .expectNext(question)
                .verifyComplete();
        
        StepVerifier.create(questionsRepository.findByActionIdAndQuestionId("test-action-id", "question-1"))
                .expectNext(question)
                .verifyComplete();
    }
    
    @Test
    void shouldSaveAndRetrieveRule() {
        RulesByFlowEntity rule = TestDataBuilder.createTestRule();
        
        StepVerifier.create(rulesByFlowRepository.save(rule))
                .expectNext(rule)
                .verifyComplete();
        
        StepVerifier.create(rulesByFlowRepository.findByFlow("test-flow"))
                .expectNext(rule)
                .verifyComplete();
    }
    
    @Test
    void shouldSaveAndRetrieveAnswerOption() {
        AnswerOptionsEntity option = TestDataBuilder.createTestAnswerOption();
        
        StepVerifier.create(answerOptionsRepository.save(option))
                .expectNext(option)
                .verifyComplete();
        
        StepVerifier.create(answerOptionsRepository.findByActionIdAndQuestionId("test-action-id", "question-1"))
                .expectNext(option)
                .verifyComplete();
    }
    
    @Test
    void shouldDeleteAction() {
        ActionsEntity action = TestDataBuilder.createTestAction();
        
        StepVerifier.create(actionsRepository.save(action))
                .expectNext(action)
                .verifyComplete();
        
        StepVerifier.create(actionsRepository.deleteByActionId("test-action-id"))
                .verifyComplete();
        
        StepVerifier.create(actionsRepository.findByActionId("test-action-id"))
                .verifyComplete();
    }
    
    @Test
    void shouldHandleComplexQuestionnaireWorkflow() {
        ActionsEntity action = TestDataBuilder.createTestAction();
        QuestionsEntity question = TestDataBuilder.createTestQuestion();
        RulesByFlowEntity rule = TestDataBuilder.createTestRule();
        AnswerOptionsEntity option = TestDataBuilder.createTestAnswerOption();
        
        StepVerifier.create(actionsRepository.save(action))
                .expectNext(action)
                .verifyComplete();
        
        StepVerifier.create(questionsRepository.save(question))
                .expectNext(question)
                .verifyComplete();
        
        StepVerifier.create(rulesByFlowRepository.save(rule))
                .expectNext(rule)
                .verifyComplete();
        
        StepVerifier.create(answerOptionsRepository.save(option))
                .expectNext(option)
                .verifyComplete();
        
        StepVerifier.create(rulesByFlowRepository.findByFlow("test-flow"))
                .expectNext(rule)
                .verifyComplete();
        
        StepVerifier.create(actionsRepository.findByActionId("test-action-id"))
                .expectNext(action)
                .verifyComplete();
        
        StepVerifier.create(questionsRepository.findByActionIdAndQuestionId("test-action-id", "question-1"))
                .expectNext(question)
                .verifyComplete();
        
        StepVerifier.create(answerOptionsRepository.findByActionIdAndQuestionId("test-action-id", "question-1"))
                .expectNext(option)
                .verifyComplete();
    }
}
