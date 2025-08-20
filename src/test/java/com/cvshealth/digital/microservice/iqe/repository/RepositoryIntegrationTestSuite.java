package com.cvshealth.digital.microservice.iqe.repository;

import com.cvshealth.digital.microservice.iqe.config.BaseIntegrationTest;
import com.cvshealth.digital.microservice.iqe.entity.ActionsEntity;
import com.cvshealth.digital.microservice.iqe.entity.AnswerOptionsEntity;
import com.cvshealth.digital.microservice.iqe.entity.QuestionsEntity;
import com.cvshealth.digital.microservice.iqe.entity.RulesByFlowEntity;
import com.cvshealth.digital.microservice.iqe.testdata.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

class RepositoryIntegrationTestSuite extends BaseIntegrationTest {
    
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
    }
    
    @Test
    void shouldPerformActionsRepositoryCRUD() {
        ActionsEntity action = TestDataBuilder.createTestAction();
        
        StepVerifier.create(actionsRepository.save(action))
                .expectNext(action)
                .verifyComplete();
        
        StepVerifier.create(actionsRepository.findByActionId("test-action-id"))
                .expectNext(action)
                .verifyComplete();
        
        action.setActionText("Updated Action Text");
        StepVerifier.create(actionsRepository.save(action))
                .expectNext(action)
                .verifyComplete();
        
        StepVerifier.create(actionsRepository.deleteByActionId("test-action-id"))
                .verifyComplete();
        
        StepVerifier.create(actionsRepository.findByActionId("test-action-id"))
                .verifyComplete();
    }
    
    @Test
    void shouldPerformQuestionsRepositoryCRUD() {
        QuestionsEntity question = TestDataBuilder.createTestQuestion();
        
        StepVerifier.create(questionsRepository.save(question))
                .expectNext(question)
                .verifyComplete();
        
        StepVerifier.create(questionsRepository.findByActionIdAndQuestionId("test-action-id", "question-1"))
                .expectNext(question)
                .verifyComplete();
        
        StepVerifier.create(questionsRepository.deleteByActionId("test-action-id"))
                .verifyComplete();
    }
    
    @Test
    void shouldPerformRulesByFlowRepositoryCRUD() {
        RulesByFlowEntity rule = TestDataBuilder.createTestRule();
        
        StepVerifier.create(rulesByFlowRepository.save(rule))
                .expectNext(rule)
                .verifyComplete();
        
        StepVerifier.create(rulesByFlowRepository.findByFlow("test-flow"))
                .expectNext(rule)
                .verifyComplete();
        
        StepVerifier.create(rulesByFlowRepository.findByActionId("test-action-id"))
                .expectNext(rule)
                .verifyComplete();
    }
    
    @Test
    void shouldPerformAnswerOptionsRepositoryCRUD() {
        AnswerOptionsEntity option = TestDataBuilder.createTestAnswerOption();
        
        StepVerifier.create(answerOptionsRepository.save(option))
                .expectNext(option)
                .verifyComplete();
        
        StepVerifier.create(answerOptionsRepository.findByActionIdAndQuestionId("test-action-id", "question-1"))
                .expectNext(option)
                .verifyComplete();
        
        StepVerifier.create(answerOptionsRepository.deleteByActionId("test-action-id"))
                .verifyComplete();
    }
    
    @Test
    void shouldFindQuestionsByActionId() {
        QuestionsEntity question1 = TestDataBuilder.createTestQuestion();
        QuestionsEntity question2 = TestDataBuilder.createTestQuestion();
        question2.setQuestionId("question-2");
        
        StepVerifier.create(questionsRepository.save(question1))
                .expectNext(question1)
                .verifyComplete();
        
        StepVerifier.create(questionsRepository.save(question2))
                .expectNext(question2)
                .verifyComplete();
        
        StepVerifier.create(questionsRepository.findByActionId("test-action-id"))
                .expectNextCount(2)
                .verifyComplete();
    }
    
    @Test
    void shouldFindAnswerOptionsByActionIdAndQuestionId() {
        AnswerOptionsEntity option1 = TestDataBuilder.createTestAnswerOption();
        AnswerOptionsEntity option2 = TestDataBuilder.createTestAnswerOption();
        option2.setAnswerOptionId("option-2");
        
        StepVerifier.create(answerOptionsRepository.save(option1))
                .expectNext(option1)
                .verifyComplete();
        
        StepVerifier.create(answerOptionsRepository.save(option2))
                .expectNext(option2)
                .verifyComplete();
        
        StepVerifier.create(answerOptionsRepository.findByActionIdAndQuestionId("test-action-id", "question-1"))
                .expectNextCount(2)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleRepositoryErrors() {
        StepVerifier.create(actionsRepository.findByActionId("non-existent-id"))
                .verifyComplete();
        
        StepVerifier.create(questionsRepository.findByActionId("non-existent-id"))
                .verifyComplete();
        
        StepVerifier.create(rulesByFlowRepository.findByFlow("non-existent-flow"))
                .verifyComplete();
    }
    
    @Test
    void shouldHandleConcurrentOperations() {
        ActionsEntity action1 = TestDataBuilder.createTestAction();
        ActionsEntity action2 = TestDataBuilder.createTestAction();
        action2.setActionId("test-action-id-2");
        
        StepVerifier.create(actionsRepository.save(action1).then(actionsRepository.save(action2)))
                .expectNext(action2)
                .verifyComplete();
        
        StepVerifier.create(actionsRepository.findByActionId("test-action-id"))
                .expectNext(action1)
                .verifyComplete();
        
        StepVerifier.create(actionsRepository.findByActionId("test-action-id-2"))
                .expectNext(action2)
                .verifyComplete();
    }
}
