package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.dto.AnswerOptions;
import com.cvshealth.digital.microservice.iqe.dto.Details;
import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
import com.cvshealth.digital.microservice.iqe.dto.Questions;
import com.cvshealth.digital.microservice.iqe.dto.RulesByFlow;
import com.cvshealth.digital.microservice.iqe.entity.AnswerOptionsEntity;
import com.cvshealth.digital.microservice.iqe.entity.QuestionsDetailsEntity;
import com.cvshealth.digital.microservice.iqe.entity.QuestionsEntity;
import com.cvshealth.digital.microservice.iqe.model.IQEResponse;
import com.cvshealth.digital.microservice.iqe.repository.ActionsRepository;
import com.cvshealth.digital.microservice.iqe.repository.AnswerOptionsRepository;
import com.cvshealth.digital.microservice.iqe.repository.QuestionnaireDetailsRepository;
import com.cvshealth.digital.microservice.iqe.repository.QuestionsRepository;
import com.cvshealth.digital.microservice.iqe.repository.RulesByFlowRepository;
import com.cvshealth.digital.microservice.iqe.testdata.TestDataBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IQERepoOrchestratorComprehensiveTest {
    
    @Mock
    private ActionsRepository actionsRepo;
    
    @Mock
    private QuestionsRepository questionsRepo;
    
    @Mock
    private RulesByFlowRepository rulesByFlowRepo;
    
    @Mock
    private QuestionnaireDetailsRepository questionnaireDetailsRepo;
    
    @Mock
    private AnswerOptionsRepository answerOptionsRepo;
    
    @Mock
    private RedisCacheService redisCacheService;
    
    @InjectMocks
    private IQERepoOrchestrator iqeRepoOrchestrator;
    
    @Test
    void shouldHandleCircularReferenceInQuestions() {
        QuestionsEntity question1 = TestDataBuilder.createTestQuestion();
        question1.setQuestionId("question-1");
        List<String> optionIds1 = new ArrayList<>();
        optionIds1.add("option-1");
        question1.setAnswerOptionId(optionIds1);
        
        QuestionsEntity question2 = TestDataBuilder.createTestQuestion();
        question2.setQuestionId("question-2");
        List<String> optionIds2 = new ArrayList<>();
        optionIds2.add("option-2");
        question2.setAnswerOptionId(optionIds2);
        
        AnswerOptionsEntity option1 = TestDataBuilder.createTestAnswerOption();
        option1.setAnswerOptionId("option-1");
        List<String> relatedQuestions1 = new ArrayList<>();
        relatedQuestions1.add("question-2");
        option1.setRelatedQuestions(relatedQuestions1);
        
        AnswerOptionsEntity option2 = TestDataBuilder.createTestAnswerOption();
        option2.setAnswerOptionId("option-2");
        option2.setRelatedQuestions(new ArrayList<>());
        
        List<AnswerOptionsEntity> answerOptions = new ArrayList<>();
        answerOptions.add(option1);
        answerOptions.add(option2);
        
        List<QuestionsEntity> questions = new ArrayList<>();
        questions.add(question1);
        questions.add(question2);
        
        StepVerifier.create(iqeRepoOrchestrator.processQuestionnaire(question1, answerOptions, questions))
                .expectNextMatches(result -> 
                    result != null && 
                    result.getQuestionId() != null)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleDeepNestedQuestionStructure() {
        QuestionsEntity question1 = TestDataBuilder.createTestQuestion();
        question1.setQuestionId("question-1");
        List<String> optionIds1 = new ArrayList<>();
        optionIds1.add("option-1");
        question1.setAnswerOptionId(optionIds1);
        
        QuestionsEntity question2 = TestDataBuilder.createTestQuestion();
        question2.setQuestionId("question-2");
        List<String> optionIds2 = new ArrayList<>();
        optionIds2.add("option-2");
        question2.setAnswerOptionId(optionIds2);
        
        QuestionsEntity question3 = TestDataBuilder.createTestQuestion();
        question3.setQuestionId("question-3");
        List<String> optionIds3 = new ArrayList<>();
        optionIds3.add("option-3");
        question3.setAnswerOptionId(optionIds3);
        
        AnswerOptionsEntity option1 = TestDataBuilder.createTestAnswerOption();
        option1.setAnswerOptionId("option-1");
        List<String> relatedQuestions1 = new ArrayList<>();
        relatedQuestions1.add("question-2");
        option1.setRelatedQuestions(relatedQuestions1);
        
        AnswerOptionsEntity option2 = TestDataBuilder.createTestAnswerOption();
        option2.setAnswerOptionId("option-2");
        List<String> relatedQuestions2 = new ArrayList<>();
        relatedQuestions2.add("question-3");
        option2.setRelatedQuestions(relatedQuestions2);
        
        AnswerOptionsEntity option3 = TestDataBuilder.createTestAnswerOption();
        option3.setAnswerOptionId("option-3");
        option3.setRelatedQuestions(new ArrayList<>());
        
        List<AnswerOptionsEntity> answerOptions = new ArrayList<>();
        answerOptions.add(option1);
        answerOptions.add(option2);
        answerOptions.add(option3);
        
        List<QuestionsEntity> questions = new ArrayList<>();
        questions.add(question1);
        questions.add(question2);
        questions.add(question3);
        
        StepVerifier.create(iqeRepoOrchestrator.processQuestionnaire(question1, answerOptions, questions))
                .expectNextMatches(result -> 
                    result != null && 
                    result.getQuestionId() != null &&
                    result.getAnswerOptions() != null &&
                    result.getAnswerOptions().size() == 1 &&
                    result.getAnswerOptions().get(0).getRelatedQuestions() != null &&
                    !result.getAnswerOptions().get(0).getRelatedQuestions().isEmpty())
                .verifyComplete();
    }
    
    @Test
    void shouldHandleMultipleRelatedQuestionsPerAnswerOption() {
        QuestionsEntity question1 = TestDataBuilder.createTestQuestion();
        question1.setQuestionId("question-1");
        List<String> optionIds1 = new ArrayList<>();
        optionIds1.add("option-1");
        question1.setAnswerOptionId(optionIds1);
        
        QuestionsEntity question2 = TestDataBuilder.createTestQuestion();
        question2.setQuestionId("question-2");
        question2.setAnswerOptionId(new ArrayList<>());
        
        QuestionsEntity question3 = TestDataBuilder.createTestQuestion();
        question3.setQuestionId("question-3");
        question3.setAnswerOptionId(new ArrayList<>());
        
        AnswerOptionsEntity option1 = TestDataBuilder.createTestAnswerOption();
        option1.setAnswerOptionId("option-1");
        List<String> relatedQuestions = new ArrayList<>();
        relatedQuestions.add("question-2");
        relatedQuestions.add("question-3");
        option1.setRelatedQuestions(relatedQuestions);
        
        List<AnswerOptionsEntity> answerOptions = new ArrayList<>();
        answerOptions.add(option1);
        
        List<QuestionsEntity> questions = new ArrayList<>();
        questions.add(question1);
        questions.add(question2);
        questions.add(question3);
        
        StepVerifier.create(iqeRepoOrchestrator.processQuestionnaire(question1, answerOptions, questions))
                .expectNextMatches(result -> 
                    result != null && 
                    result.getQuestionId() != null &&
                    result.getAnswerOptions() != null &&
                    result.getAnswerOptions().size() == 1 &&
                    result.getAnswerOptions().get(0).getRelatedQuestions() != null &&
                    result.getAnswerOptions().get(0).getRelatedQuestions().size() == 2)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleNonExistentRelatedQuestions() {
        QuestionsEntity question = TestDataBuilder.createTestQuestion();
        question.setQuestionId("question-1");
        List<String> optionIds = new ArrayList<>();
        optionIds.add("option-1");
        question.setAnswerOptionId(optionIds);
        
        AnswerOptionsEntity option = TestDataBuilder.createTestAnswerOption();
        option.setAnswerOptionId("option-1");
        List<String> relatedQuestions = new ArrayList<>();
        relatedQuestions.add("non-existent-question");
        option.setRelatedQuestions(relatedQuestions);
        
        List<AnswerOptionsEntity> answerOptions = new ArrayList<>();
        answerOptions.add(option);
        
        List<QuestionsEntity> questions = new ArrayList<>();
        questions.add(question);
        
        StepVerifier.create(iqeRepoOrchestrator.processQuestionnaire(question, answerOptions, questions))
                .expectNextMatches(result -> 
                    result != null && 
                    result.getQuestionId() != null &&
                    result.getAnswerOptions() != null &&
                    result.getAnswerOptions().size() == 1 &&
                    (result.getAnswerOptions().get(0).getRelatedQuestions() == null || 
                     result.getAnswerOptions().get(0).getRelatedQuestions().isEmpty()))
                .verifyComplete();
    }
    
    @Test
    void shouldHandleNullQuestionnaireRequest() {
        Map<String, String> reqHdrMap = new HashMap<>();
        IQEResponse iqeResponse = new IQEResponse();
        Map<String, Object> eventMap = new HashMap<>();
        
        StepVerifier.create(iqeRepoOrchestrator.processInputData(null, reqHdrMap, iqeResponse, eventMap))
                .expectError(NullPointerException.class)
                .verify();
    }
    
    @Test
    void shouldHandleEmptyQuestionsList() {
        QuestionareRequest request = TestDataBuilder.createTestQuestionareRequest();
        request.setQuestions(new ArrayList<>());
        Map<String, String> reqHdrMap = new HashMap<>();
        IQEResponse iqeResponse = new IQEResponse();
        Map<String, Object> eventMap = new HashMap<>();
        
        StepVerifier.create(iqeRepoOrchestrator.processInputData(request, reqHdrMap, iqeResponse, eventMap))
                .expectNextMatches(result -> 
                    result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleNullQuestionsList() {
        QuestionareRequest request = TestDataBuilder.createTestQuestionareRequest();
        request.setQuestions(null);
        Map<String, String> reqHdrMap = new HashMap<>();
        IQEResponse iqeResponse = new IQEResponse();
        Map<String, Object> eventMap = new HashMap<>();
        
        StepVerifier.create(iqeRepoOrchestrator.processInputData(request, reqHdrMap, iqeResponse, eventMap))
                .expectNextMatches(result -> 
                    result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleRepositoryErrorsInInsertQuestionsIntoDB() {
        QuestionareRequest request = TestDataBuilder.createTestQuestionareRequest();
        Map<String, Object> eventMap = new HashMap<>();
        IQEResponse iqeResponse = new IQEResponse();
        
        when(rulesByFlowRepo.save(any())).thenReturn(Mono.error(new RuntimeException("Database error")));
        
        StepVerifier.create(iqeRepoOrchestrator.insertQuestionsIntoDB(request, eventMap, iqeResponse))
                .expectError(RuntimeException.class)
                .verify();
    }
    
    @Test
    void shouldHandleComplexNestedStructureInExtractQuestionsRecursive() {
        com.cvshealth.digital.microservice.iqe.dto.Questions parentQuestion = TestDataBuilder.createTestQuestionDto();
        parentQuestion.setQuestionId("parent");
        
        AnswerOptions parentOption1 = TestDataBuilder.createTestAnswerOptionDTO();
        parentOption1.setAnswerOptionId("parent-option-1");
        
        AnswerOptions parentOption2 = TestDataBuilder.createTestAnswerOptionDTO();
        parentOption2.setAnswerOptionId("parent-option-2");
        
        com.cvshealth.digital.microservice.iqe.dto.Questions childQuestion1 = TestDataBuilder.createTestQuestionDto();
        childQuestion1.setQuestionId("child-1");
        
        com.cvshealth.digital.microservice.iqe.dto.Questions childQuestion2 = TestDataBuilder.createTestQuestionDto();
        childQuestion2.setQuestionId("child-2");
        
        AnswerOptions childOption1 = TestDataBuilder.createTestAnswerOptionDTO();
        childOption1.setAnswerOptionId("child-option-1");
        
        AnswerOptions childOption2 = TestDataBuilder.createTestAnswerOptionDTO();
        childOption2.setAnswerOptionId("child-option-2");
        
        List<AnswerOptions> childOptions1 = new ArrayList<>();
        childOptions1.add(childOption1);
        childQuestion1.setAnswerOptions(childOptions1);
        
        List<AnswerOptions> childOptions2 = new ArrayList<>();
        childOptions2.add(childOption2);
        childQuestion2.setAnswerOptions(childOptions2);
        
        List<com.cvshealth.digital.microservice.iqe.dto.Questions> relatedQuestions1 = new ArrayList<>();
        relatedQuestions1.add(childQuestion1);
        parentOption1.setRelatedQuestions(relatedQuestions1);
        
        List<com.cvshealth.digital.microservice.iqe.dto.Questions> relatedQuestions2 = new ArrayList<>();
        relatedQuestions2.add(childQuestion2);
        parentOption2.setRelatedQuestions(relatedQuestions2);
        
        List<AnswerOptions> parentOptions = new ArrayList<>();
        parentOptions.add(parentOption1);
        parentOptions.add(parentOption2);
        parentQuestion.setAnswerOptions(parentOptions);
        
        StepVerifier.create(iqeRepoOrchestrator.extractQuestionsRecursive(parentQuestion))
                .expectNextCount(3) // Parent + 2 children
                .verifyComplete();
    }
    
    @Test
    void shouldHandleComplexNestedStructureInExtractAnswerOptionsRecursive() {
        com.cvshealth.digital.microservice.iqe.dto.Questions parentQuestion = TestDataBuilder.createTestQuestionDto();
        parentQuestion.setQuestionId("parent");
        
        AnswerOptions parentOption1 = TestDataBuilder.createTestAnswerOptionDTO();
        parentOption1.setAnswerOptionId("parent-option-1");
        
        AnswerOptions parentOption2 = TestDataBuilder.createTestAnswerOptionDTO();
        parentOption2.setAnswerOptionId("parent-option-2");
        
        com.cvshealth.digital.microservice.iqe.dto.Questions childQuestion1 = TestDataBuilder.createTestQuestionDto();
        childQuestion1.setQuestionId("child-1");
        
        com.cvshealth.digital.microservice.iqe.dto.Questions childQuestion2 = TestDataBuilder.createTestQuestionDto();
        childQuestion2.setQuestionId("child-2");
        
        AnswerOptions childOption1 = TestDataBuilder.createTestAnswerOptionDTO();
        childOption1.setAnswerOptionId("child-option-1");
        
        AnswerOptions childOption2 = TestDataBuilder.createTestAnswerOptionDTO();
        childOption2.setAnswerOptionId("child-option-2");
        
        List<AnswerOptions> childOptions1 = new ArrayList<>();
        childOptions1.add(childOption1);
        childQuestion1.setAnswerOptions(childOptions1);
        
        List<AnswerOptions> childOptions2 = new ArrayList<>();
        childOptions2.add(childOption2);
        childQuestion2.setAnswerOptions(childOptions2);
        
        List<com.cvshealth.digital.microservice.iqe.dto.Questions> relatedQuestions1 = new ArrayList<>();
        relatedQuestions1.add(childQuestion1);
        parentOption1.setRelatedQuestions(relatedQuestions1);
        
        List<com.cvshealth.digital.microservice.iqe.dto.Questions> relatedQuestions2 = new ArrayList<>();
        relatedQuestions2.add(childQuestion2);
        parentOption2.setRelatedQuestions(relatedQuestions2);
        
        List<AnswerOptions> parentOptions = new ArrayList<>();
        parentOptions.add(parentOption1);
        parentOptions.add(parentOption2);
        parentQuestion.setAnswerOptions(parentOptions);
        
        StepVerifier.create(iqeRepoOrchestrator.extractAnswerOptionsRecursive(parentQuestion))
                .expectNextCount(4) // 2 parent options + 2 child options
                .verifyComplete();
    }
    
    @Test
    void shouldHandleQuestionWithMultipleAnswerOptions() {
        com.cvshealth.digital.microservice.iqe.dto.Questions question = TestDataBuilder.createTestQuestionDto();
        question.setQuestionId("test-question");
        
        AnswerOptions option1 = TestDataBuilder.createTestAnswerOptionDTO();
        option1.setAnswerOptionId("option-1");
        
        AnswerOptions option2 = TestDataBuilder.createTestAnswerOptionDTO();
        option2.setAnswerOptionId("option-2");
        
        List<AnswerOptions> options = new ArrayList<>();
        options.add(option1);
        options.add(option2);
        question.setAnswerOptions(options);
        
        StepVerifier.create(iqeRepoOrchestrator.processQuestion(question, false, "test-action-id"))
                .expectNextMatches(result -> 
                    result != null && 
                    result.getQuestionId() != null &&
                    result.getActionId().equals("test-action-id"))
                .verifyComplete();
    }
    
    @Test
    void shouldHandleAdditionalPropertiesInProcessQuestion() {
        com.cvshealth.digital.microservice.iqe.dto.Questions question = TestDataBuilder.createTestQuestionDto();
        question.setQuestionId("test-question");
        question.setHelpText("Test help text");
        question.setCharacterLimit(100);
        
        StepVerifier.create(iqeRepoOrchestrator.processQuestion(question, false, "test-action-id"))
                .expectNextMatches(result -> 
                    result != null && 
                    result.getQuestionId() != null &&
                    result.getActionId().equals("test-action-id"))
                .verifyComplete();
    }
    
    @Test
    void shouldHandleNullAnswerOptionsInProcessQuestion() {
        com.cvshealth.digital.microservice.iqe.dto.Questions question = TestDataBuilder.createTestQuestionDto();
        question.setQuestionId("test-question");
        question.setAnswerOptions(null);
        
        StepVerifier.create(iqeRepoOrchestrator.processQuestion(question, false, "test-action-id"))
                .expectNextMatches(result -> 
                    result != null && 
                    result.getQuestionId() != null &&
                    result.getActionId().equals("test-action-id"))
                .verifyComplete();
    }
    
    @Test
    void shouldHandleEmptyAnswerOptionsInProcessQuestion() {
        com.cvshealth.digital.microservice.iqe.dto.Questions question = TestDataBuilder.createTestQuestionDto();
        question.setQuestionId("test-question");
        question.setAnswerOptions(new ArrayList<>());
        
        StepVerifier.create(iqeRepoOrchestrator.processQuestion(question, false, "test-action-id"))
                .expectNextMatches(result -> 
                    result != null && 
                    result.getQuestionId() != null &&
                    result.getActionId().equals("test-action-id"))
                .verifyComplete();
    }
    
    @Test
    void shouldHandleNullRelatedQuestionsInAnswerOptions() {
        QuestionsEntity question = TestDataBuilder.createTestQuestion();
        question.setQuestionId("test-question-id");
        
        AnswerOptionsEntity answerOption = TestDataBuilder.createTestAnswerOption();
        answerOption.setAnswerOptionId("test-option");
        answerOption.setRelatedQuestions(null);
        
        List<AnswerOptionsEntity> answerOptions = new ArrayList<>();
        answerOptions.add(answerOption);
        
        List<QuestionsEntity> questions = new ArrayList<>();
        questions.add(question);
        
        StepVerifier.create(iqeRepoOrchestrator.processQuestionnaire(question, answerOptions, questions))
                .expectNextMatches(result -> 
                    result != null && 
                    result.getQuestionId() != null)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleEmptyRelatedQuestionsInAnswerOptions() {
        QuestionsEntity question = TestDataBuilder.createTestQuestion();
        question.setQuestionId("test-question-id");
        
        AnswerOptionsEntity answerOption = TestDataBuilder.createTestAnswerOption();
        answerOption.setAnswerOptionId("test-option");
        answerOption.setRelatedQuestions(new ArrayList<>());
        
        List<AnswerOptionsEntity> answerOptions = new ArrayList<>();
        answerOptions.add(answerOption);
        
        List<QuestionsEntity> questions = new ArrayList<>();
        questions.add(question);
        
        StepVerifier.create(iqeRepoOrchestrator.processQuestionnaire(question, answerOptions, questions))
                .expectNextMatches(result -> 
                    result != null && 
                    result.getQuestionId() != null)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleNullRulesByFlowInRequest() {
        QuestionareRequest request = TestDataBuilder.createTestQuestionareRequest();
        request.setRulesByFlow(null);
        Map<String, String> reqHdrMap = new HashMap<>();
        IQEResponse iqeResponse = new IQEResponse();
        Map<String, Object> eventMap = new HashMap<>();
        
        StepVerifier.create(iqeRepoOrchestrator.processInputData(request, reqHdrMap, iqeResponse, eventMap))
                .expectError(NullPointerException.class)
                .verify();
    }
    
    @Test
    void shouldHandleNullActionsInRequest() {
        QuestionareRequest request = TestDataBuilder.createTestQuestionareRequest();
        request.setActions(null);
        Map<String, String> reqHdrMap = new HashMap<>();
        IQEResponse iqeResponse = new IQEResponse();
        Map<String, Object> eventMap = new HashMap<>();
        
        StepVerifier.create(iqeRepoOrchestrator.processInputData(request, reqHdrMap, iqeResponse, eventMap))
                .expectError(NullPointerException.class)
                .verify();
    }
    
    @Test
    void shouldHandleNullAuditInRulesByFlow() {
        RulesByFlow rulesByFlow = TestDataBuilder.createTestRulesByFlow();
        rulesByFlow.setAudit(null);
        Map<String, String> reqHdrMap = new HashMap<>();
        
        iqeRepoOrchestrator.setAuditData(rulesByFlow, reqHdrMap);
        
        assertThat(rulesByFlow.getAudit()).isNotNull();
        assertThat(rulesByFlow.getAudit().getCreatedBy()).isEqualTo("Default user");
    }
}
