package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.dto.Questions;
import com.cvshealth.digital.microservice.iqe.entity.ActionsEntity;
import com.cvshealth.digital.microservice.iqe.entity.QuestionsEntity;
import com.cvshealth.digital.microservice.iqe.entity.AnswerOptionsEntity;
import com.cvshealth.digital.microservice.iqe.repository.ActionsRepository;
import com.cvshealth.digital.microservice.iqe.repository.QuestionsRepository;
import com.cvshealth.digital.microservice.iqe.repository.AnswerOptionsRepository;
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
import java.util.List;
import java.util.Map;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class IQERepoOrchestratorIntegrationTest {
    
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
    private IQERepoOrchestrator iqeRepoOrchestrator;
    
    @Autowired
    private ActionsRepository actionsRepository;
    
    @Autowired
    private QuestionsRepository questionsRepository;
    
    @Autowired
    private AnswerOptionsRepository answerOptionsRepository;
    
    @BeforeEach
    void setUp() {
        actionsRepository.deleteAll().block();
        questionsRepository.deleteAll().block();
        answerOptionsRepository.deleteAll().block();
    }
    
    @Test
    void shouldProcessQuestionnaireWithRealData() {
        ActionsEntity action = TestDataBuilder.createTestAction();
        QuestionsEntity question = TestDataBuilder.createTestQuestion();
        AnswerOptionsEntity option = TestDataBuilder.createTestAnswerOption();
        
        actionsRepository.save(action).block();
        questionsRepository.save(question).block();
        answerOptionsRepository.save(option).block();
        
        List<AnswerOptionsEntity> answerOptionsList = List.of(option);
        List<QuestionsEntity> questions = List.of(question);
        
        StepVerifier.create(iqeRepoOrchestrator.processQuestionnaire(question, answerOptionsList, questions))
                .expectNextMatches(result -> 
                    result != null && 
                    result.getQuestionId() != null)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleEmptyAnswerOptions() {
        QuestionsEntity question = TestDataBuilder.createTestQuestion();
        questionsRepository.save(question).block();
        
        List<AnswerOptionsEntity> answerOptionsList = List.of();
        List<QuestionsEntity> questions = List.of(question);
        
        StepVerifier.create(iqeRepoOrchestrator.processQuestionnaire(question, answerOptionsList, questions))
                .expectNextMatches(result -> 
                    result != null && 
                    result.getQuestionId() != null &&
                    (result.getAnswerOptions() == null || result.getAnswerOptions().isEmpty()))
                .verifyComplete();
    }
    
    @Test
    void shouldHandleMultipleAnswerOptions() {
        ActionsEntity action1 = TestDataBuilder.createTestAction();
        ActionsEntity action2 = TestDataBuilder.createTestAction();
        action2.setActionId("test-action-id-2");
        
        QuestionsEntity question1 = TestDataBuilder.createTestQuestion();
        QuestionsEntity question2 = TestDataBuilder.createTestQuestion();
        question2.setActionId("test-action-id-2");
        question2.setQuestionId("question-2");
        
        AnswerOptionsEntity option1 = TestDataBuilder.createTestAnswerOption();
        AnswerOptionsEntity option2 = TestDataBuilder.createTestAnswerOption();
        option2.setActionId("test-action-id-2");
        option2.setQuestionId("question-2");
        option2.setAnswerOptionId("option-2");
        
        actionsRepository.save(action1).block();
        actionsRepository.save(action2).block();
        questionsRepository.save(question1).block();
        questionsRepository.save(question2).block();
        answerOptionsRepository.save(option1).block();
        answerOptionsRepository.save(option2).block();
        
        List<AnswerOptionsEntity> answerOptionsList = List.of(option1, option2);
        List<QuestionsEntity> questions = List.of(question1, question2);
        
        StepVerifier.create(iqeRepoOrchestrator.processQuestionnaire(question1, answerOptionsList, questions))
                .expectNextMatches(result -> 
                    result != null && 
                    result.getQuestionId() != null &&
                    result.getAnswerOptions() != null)
                .verifyComplete();
    }
}
