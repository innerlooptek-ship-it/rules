package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.dto.AnswerOptions;
import com.cvshealth.digital.microservice.iqe.dto.Details;
import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
import com.cvshealth.digital.microservice.iqe.dto.Questions;
import com.cvshealth.digital.microservice.iqe.entity.AnswerOptionsEntity;
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
class IQERepoOrchestratorUnitTest {
    
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
    void shouldProcessQuestionnaireWithValidData() {
        QuestionsEntity question = TestDataBuilder.createTestQuestion();
        question.setAnswerOptionId(List.of("test-answer-option-id"));
        
        AnswerOptionsEntity answerOption = TestDataBuilder.createTestAnswerOption();
        answerOption.setAnswerOptionId("test-answer-option-id");
        answerOption.setRelatedQuestions(List.of());
        
        List<AnswerOptionsEntity> answerOptions = new ArrayList<>(List.of(answerOption));
        List<QuestionsEntity> questions = new ArrayList<>(List.of(question));
        
        StepVerifier.create(iqeRepoOrchestrator.processQuestionnaire(question, answerOptions, questions))
                .expectNextMatches(result -> 
                    result != null && 
                    result.getQuestionId() != null)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleEmptyAnswerOptionsList() {
        QuestionsEntity question = TestDataBuilder.createTestQuestion();
        question.setAnswerOptionId(List.of());
        
        List<AnswerOptionsEntity> answerOptions = new ArrayList<>();
        List<QuestionsEntity> questions = new ArrayList<>(List.of(question));
        
        StepVerifier.create(iqeRepoOrchestrator.processQuestionnaire(question, answerOptions, questions))
                .expectNextMatches(result -> 
                    result != null && 
                    result.getQuestionId() != null &&
                    (result.getAnswerOptions() == null || result.getAnswerOptions().isEmpty()))
                .verifyComplete();
    }
    
    @Test
    void shouldHandleNullAnswerOptions() {
        QuestionsEntity question = TestDataBuilder.createTestQuestion();
        question.setAnswerOptionId(null);
        
        List<QuestionsEntity> questions = new ArrayList<>(List.of(question));
        
        StepVerifier.create(iqeRepoOrchestrator.processQuestionnaire(question, new ArrayList<>(), questions))
                .expectNextMatches(result -> 
                    result != null && 
                    result.getQuestionId() != null)
                .verifyComplete();
    }
    
    @Test
    void shouldProcessMultipleAnswerOptions() {
        QuestionsEntity question = TestDataBuilder.createTestQuestion();
        question.setAnswerOptionId(List.of("option-1", "option-2"));
        
        AnswerOptionsEntity option1 = TestDataBuilder.createTestAnswerOption();
        option1.setAnswerOptionId("option-1");
        option1.setRelatedQuestions(List.of());
        
        AnswerOptionsEntity option2 = TestDataBuilder.createTestAnswerOption();
        option2.setAnswerOptionId("option-2");
        option2.setRelatedQuestions(List.of());
        
        List<AnswerOptionsEntity> answerOptions = new ArrayList<>(List.of(option1, option2));
        List<QuestionsEntity> questions = new ArrayList<>(List.of(question));
        
        StepVerifier.create(iqeRepoOrchestrator.processQuestionnaire(question, answerOptions, questions))
                .expectNextMatches(result -> 
                    result != null && 
                    result.getQuestionId() != null &&
                    result.getAnswerOptions() != null &&
                    result.getAnswerOptions().size() == 2)
                .verifyComplete();
    }
    
    @Test
    void shouldGenerateUUID() {
        String uuid = iqeRepoOrchestrator.generateUUID();
        
        assert uuid != null;
        assert !uuid.isEmpty();
        assert uuid.contains("-");
    }
    
    @Test
    void shouldProcessInputDataSuccessfully() {
        QuestionareRequest request = TestDataBuilder.createTestQuestionareRequest();
        Map<String, String> reqHdrMap = new HashMap<>();
        reqHdrMap.put("user", "test-user");
        IQEResponse iqeResponse = new IQEResponse();
        Map<String, Object> eventMap = new HashMap<>();
        
        StepVerifier.create(iqeRepoOrchestrator.processInputData(request, reqHdrMap, iqeResponse, eventMap))
                .expectNextMatches(result -> result != null)
                .verifyComplete();
    }
    
    @Test
    void shouldProcessQuestionWithAnswerOptions() {
        com.cvshealth.digital.microservice.iqe.dto.Questions question = TestDataBuilder.createTestQuestionDto();
        AnswerOptions answerOption = TestDataBuilder.createTestAnswerOptionDTO();
        question.setAnswerOptions(List.of(answerOption));
        
        StepVerifier.create(iqeRepoOrchestrator.processQuestion(question, false, "test-action-id"))
                .expectNextMatches(result -> 
                    result != null && 
                    result.getQuestionId() != null &&
                    result.getActionId().equals("test-action-id") &&
                    result.getAnswerOptions() != null &&
                    !result.getAnswerOptions().isEmpty())
                .verifyComplete();
    }
    
    @Test
    void shouldProcessQuestionWithoutAnswerOptions() {
        com.cvshealth.digital.microservice.iqe.dto.Questions question = TestDataBuilder.createTestQuestionDto();
        question.setAnswerOptions(null);
        
        StepVerifier.create(iqeRepoOrchestrator.processQuestion(question, false, "test-action-id"))
                .expectNextMatches(result -> 
                    result != null && 
                    result.getQuestionId() != null &&
                    result.getActionId().equals("test-action-id"))
                .verifyComplete();
    }
    
    @Test
    void shouldProcessDetailSuccessfully() {
        Details detail = TestDataBuilder.createTestDetail();
        
        StepVerifier.create(iqeRepoOrchestrator.processDetail(detail, "test-action-id", 1))
                .expectNextMatches(result -> 
                    result != null && 
                    result.getDetailId() != null &&
                    result.getActionId().equals("test-action-id") &&
                    result.getSequenceId() == 1)
                .verifyComplete();
    }
    
    @Test
    void shouldInsertQuestionsIntoDBSuccessfully() {
        QuestionareRequest request = TestDataBuilder.createTestQuestionareRequest();
        Map<String, Object> eventMap = new HashMap<>();
        IQEResponse iqeResponse = new IQEResponse();
        
        when(rulesByFlowRepo.save(any())).thenReturn(Mono.just(TestDataBuilder.createTestRule()));
        when(actionsRepo.save(any())).thenReturn(Mono.just(TestDataBuilder.createTestAction()));
        when(questionsRepo.save(any())).thenReturn(Mono.just(TestDataBuilder.createTestQuestion()));
        when(answerOptionsRepo.save(any())).thenReturn(Mono.just(TestDataBuilder.createTestAnswerOption()));
        when(questionnaireDetailsRepo.save(any())).thenReturn(Mono.just(TestDataBuilder.createTestQuestionnaireDetail()));
        when(redisCacheService.setDataToRedisRest(any(), any(), any())).thenReturn(Mono.empty());
        
        StepVerifier.create(iqeRepoOrchestrator.insertQuestionsIntoDB(request, eventMap, iqeResponse))
                .verifyComplete();
    }
    
    @Test
    void shouldAssignSequenceIdsSuccessfully() {
        QuestionareRequest request = TestDataBuilder.createTestQuestionareRequest();
        com.cvshealth.digital.microservice.iqe.dto.Questions question = TestDataBuilder.createTestQuestionDto();
        AnswerOptions answerOption = TestDataBuilder.createTestAnswerOptionDTO();
        question.setAnswerOptions(List.of(answerOption));
        request.setQuestions(List.of(question));
        
        StepVerifier.create(iqeRepoOrchestrator.assignSequenceIds(request))
                .expectNextMatches(result -> 
                    result != null && 
                    result.getQuestions() != null &&
                    !result.getQuestions().isEmpty() &&
                    result.getQuestions().get(0).getSequenceId() == 1)
                .verifyComplete();
    }
    
    @Test
    void shouldProcessAnswerOptionWithRelatedQuestions() {
        AnswerOptionsEntity answerOption = TestDataBuilder.createTestAnswerOption();
        answerOption.setRelatedQuestions(List.of("related-question-1"));
        answerOption.setAnswerOptionId("test-answer-option-id");
        
        QuestionsEntity relatedQuestion = TestDataBuilder.createTestQuestion();
        relatedQuestion.setQuestionId("related-question-1");
        relatedQuestion.setAnswerOptionId(List.of()); // Prevent circular reference
        
        List<AnswerOptionsEntity> answerOptionsList = List.of(answerOption);
        List<QuestionsEntity> questions = List.of(relatedQuestion);
        
        StepVerifier.create(iqeRepoOrchestrator.processAnswerOption(answerOption, answerOptionsList, questions))
                .expectNextMatches(result -> 
                    result != null && 
                    result.getAnswerOptionId() != null)
                .verifyComplete();
    }
    
    @Test
    void shouldProcessRelatedQuestionSuccessfully() {
        QuestionsEntity relatedQuestion = TestDataBuilder.createTestQuestion();
        relatedQuestion.setAnswerOptionId(List.of("answer-option-1"));
        
        AnswerOptionsEntity answerOption = TestDataBuilder.createTestAnswerOption();
        answerOption.setAnswerOptionId("answer-option-1");
        
        List<AnswerOptionsEntity> answerOptionsList = List.of(answerOption);
        List<QuestionsEntity> questions = List.of(relatedQuestion);
        
        StepVerifier.create(iqeRepoOrchestrator.processRelatedQuestion(relatedQuestion, answerOptionsList, questions))
                .expectNextMatches(result -> 
                    result != null && 
                    result.getQuestionId() != null &&
                    result.getAnswerOptions() != null)
                .verifyComplete();
    }
    
    @Test
    void shouldExtractQuestionsRecursivelySuccessfully() {
        com.cvshealth.digital.microservice.iqe.dto.Questions question = TestDataBuilder.createTestQuestionDto();
        question.setQuestionId("parent-question");
        
        com.cvshealth.digital.microservice.iqe.dto.AnswerOptions answerOption = TestDataBuilder.createTestAnswerOptionDTO();
        com.cvshealth.digital.microservice.iqe.dto.Questions nestedQuestion = TestDataBuilder.createTestQuestionDto();
        nestedQuestion.setQuestionId("nested-question");
        answerOption.setRelatedQuestions(List.of(nestedQuestion));
        question.setAnswerOptions(List.of(answerOption));
        
        StepVerifier.create(iqeRepoOrchestrator.extractQuestionsRecursive(question))
                .expectNextCount(2)
                .verifyComplete();
    }
    
    @Test
    void shouldExtractAnswerOptionsRecursivelySuccessfully() {
        com.cvshealth.digital.microservice.iqe.dto.Questions question = TestDataBuilder.createTestQuestionDto();
        question.setQuestionId("test-question");
        
        com.cvshealth.digital.microservice.iqe.dto.AnswerOptions answerOption1 = TestDataBuilder.createTestAnswerOptionDTO();
        answerOption1.setAnswerOptionId("option-1");
        
        com.cvshealth.digital.microservice.iqe.dto.AnswerOptions answerOption2 = TestDataBuilder.createTestAnswerOptionDTO();
        answerOption2.setAnswerOptionId("option-2");
        
        question.setAnswerOptions(List.of(answerOption1, answerOption2));
        
        StepVerifier.create(iqeRepoOrchestrator.extractAnswerOptionsRecursive(question))
                .expectNextCount(2)
                .verifyComplete();
    }
    
    @Test
    void shouldSetAuditDataSuccessfully() {
        com.cvshealth.digital.microservice.iqe.dto.RulesByFlow rulesByFlow = TestDataBuilder.createTestRulesByFlow();
        Map<String, String> reqHdrMap = new HashMap<>();
        reqHdrMap.put("user_id", "test-user");
        reqHdrMap.put("source", "test-source");
        
        iqeRepoOrchestrator.setAuditData(rulesByFlow, reqHdrMap);
        
        assertThat(rulesByFlow.getAudit()).isNotNull();
        assertThat(rulesByFlow.getAudit().getCreatedBy()).isEqualTo("test-user");
    }
    
    @Test
    void shouldSetAuditDataWithDefaultUser() {
        com.cvshealth.digital.microservice.iqe.dto.RulesByFlow rulesByFlow = TestDataBuilder.createTestRulesByFlow();
        Map<String, String> reqHdrMap = new HashMap<>();
        
        iqeRepoOrchestrator.setAuditData(rulesByFlow, reqHdrMap);
        
        assert rulesByFlow.getAudit() != null;
        assert rulesByFlow.getAudit().getCreatedBy().equals("Default user");
    }
    
    @Test
    void shouldHandleNullQuestionInProcessQuestion() {
        StepVerifier.create(iqeRepoOrchestrator.processQuestion(null, false, "test-action-id"))
                .expectError(NullPointerException.class)
                .verify();
    }
    
    @Test
    void shouldHandleNullDetailInProcessDetail() {
        StepVerifier.create(iqeRepoOrchestrator.processDetail(null, "test-action-id", 1))
                .expectError(NullPointerException.class)
                .verify();
    }
    
    @Test
    void shouldHandleEmptyQuestionsList() {
        QuestionareRequest request = TestDataBuilder.createTestQuestionareRequest();
        request.setQuestions(new ArrayList<>());
        
        StepVerifier.create(iqeRepoOrchestrator.assignSequenceIds(request))
                .expectNextMatches(result -> 
                    result != null && 
                    result.getQuestions().isEmpty())
                .verifyComplete();
    }
    
    @Test
    void shouldHandleNullAnswerOptionsInProcessQuestion() {
        com.cvshealth.digital.microservice.iqe.dto.Questions question = TestDataBuilder.createTestQuestionDto();
        question.setAnswerOptions(null);
        
        StepVerifier.create(iqeRepoOrchestrator.processQuestion(question, true, "test-action-id"))
                .expectNextMatches(result -> 
                    result != null && 
                    result.getQuestionId() != null &&
                    result.getActionId().equals("test-action-id"))
                .verifyComplete();
    }
    
    @Test
    void shouldHandleRedisErrorInInsertQuestionsIntoDB() {
        QuestionareRequest request = TestDataBuilder.createTestQuestionareRequest();
        Map<String, Object> eventMap = new HashMap<>();
        IQEResponse iqeResponse = new IQEResponse();
        
        when(rulesByFlowRepo.save(any())).thenReturn(Mono.just(TestDataBuilder.createTestRule()));
        when(actionsRepo.save(any())).thenReturn(Mono.just(TestDataBuilder.createTestAction()));
        when(questionsRepo.save(any())).thenReturn(Mono.just(TestDataBuilder.createTestQuestion()));
        when(answerOptionsRepo.save(any())).thenReturn(Mono.just(TestDataBuilder.createTestAnswerOption()));
        when(questionnaireDetailsRepo.save(any())).thenReturn(Mono.just(TestDataBuilder.createTestQuestionnaireDetail()));
        when(redisCacheService.setDataToRedisRest(any(), any(), any()))
                .thenReturn(Mono.error(new RuntimeException("Redis error")));
        
        StepVerifier.create(iqeRepoOrchestrator.insertQuestionsIntoDB(request, eventMap, iqeResponse))
                .expectError(RuntimeException.class)
                .verify();
    }
    
    @Test
    void shouldHandleComplexNestedQuestionStructure() {
        com.cvshealth.digital.microservice.iqe.dto.Questions parentQuestion = TestDataBuilder.createTestQuestionDto();
        parentQuestion.setQuestionId("parent");
        
        AnswerOptions parentOption = TestDataBuilder.createTestAnswerOptionDTO();
        parentOption.setAnswerOptionId("parent-option");
        
        com.cvshealth.digital.microservice.iqe.dto.Questions childQuestion = TestDataBuilder.createTestQuestionDto();
        childQuestion.setQuestionId("child");
        
        AnswerOptions childOption = TestDataBuilder.createTestAnswerOptionDTO();
        childOption.setAnswerOptionId("child-option");
        childQuestion.setAnswerOptions(List.of(childOption));
        
        parentOption.setRelatedQuestions(List.of(childQuestion));
        parentQuestion.setAnswerOptions(List.of(parentOption));
        
        StepVerifier.create(iqeRepoOrchestrator.processQuestion(parentQuestion, false, "test-action-id"))
                .expectNextMatches(result -> 
                    result != null && 
                    result.getQuestionId().equals("parent") &&
                    result.getAnswerOptions() != null &&
                    !result.getAnswerOptions().isEmpty())
                .verifyComplete();
    }
}
