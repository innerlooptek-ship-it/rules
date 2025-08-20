package com.cvshealth.digital.microservice.iqe.service;

import com.cvshealth.digital.microservice.iqe.dto.*;
import com.cvshealth.digital.microservice.iqe.error.ServerErrorException;
import com.cvshealth.digital.microservice.iqe.model.IQEResponse;
import com.cvshealth.digital.microservice.iqe.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class IQERepoOrchestratorTest {

    @Mock private ActionsRepository actionsRepo;
    @Mock private QuestionsRepository questionsRepo;
    @Mock private RulesByFlowRepository rulesByFlowRepo;
    @Mock private QuestionnaireDetailsRepository questionnaireDetailsRepo;
    @Mock private AnswerOptionsRepository answerOptionsRepo;
    @Mock private RedisCacheService redisCacheService;

    @InjectMocks
    private IQERepoOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void processInputData_success() {
        QuestionareRequest req = new QuestionareRequest();
        req.setRulesByFlow(RulesByFlow.builder().build());
        req.setActions(Actions.builder().build());
        req.setQuestions(new ArrayList<>());
        req.setDetails(new ArrayList<>());
        Map<String, String> hdr = new HashMap<>();
        IQEResponse resp = new IQEResponse();
        Map<String, Object> eventMap = new HashMap<>();

        StepVerifier.create(orchestrator.processInputData(req, hdr, resp, eventMap))
                .expectNextMatches(r -> r.getRulesByFlow().getRuleId() != null)
                .verifyComplete();
    }

    @Test
    void processInputData_error() {
        QuestionareRequest req = new QuestionareRequest();
        req.setRulesByFlow(RulesByFlow.builder().build());
        req.setActions(Actions.builder().build());
        req.setQuestions(null); // Will be initialized
        req.setDetails(null);   // Will be initialized
        Map<String, String> hdr = new HashMap<>();
        IQEResponse resp = new IQEResponse();
        Map<String, Object> eventMap = new HashMap<>();

        // Simulate error in processQuestion
        IQERepoOrchestrator spy = Mockito.spy(orchestrator);
        doReturn(Mono.error(new RuntimeException("fail"))).when(spy).processQuestion(any(), anyBoolean(), anyString());

        StepVerifier.create(spy.processInputData(req, hdr, resp, eventMap))
                .expectNextMatches(r -> r != null /* add more checks if needed, e.g. r.getErrorDescription() != null */)
                .verifyComplete();
    }

    @Test
    void processDetail_assignsIds() {
        Details detail = Details.builder().build();
        StepVerifier.create(orchestrator.processDetail(detail, "actionId", 1))
                .expectNextMatches(d -> d.getActionId().equals("actionId") && d.getSequenceId() == 1 && d.getDetailId() != null)
                .verifyComplete();
    }

    @Test
    void processQuestion_assignsIds() {
        Questions q = Questions.builder().answerOptions(new ArrayList<>()).build();
        StepVerifier.create(orchestrator.processQuestion(q, false, "actionId"))
                .expectNextMatches(res -> res.getActionId().equals("actionId") && res.getQuestionId() != null)
                .verifyComplete();
    }

 /*   @Test
    void insertQuestionsIntoDB_success() {
        QuestionareRequest req = new QuestionareRequest();
        req.setRulesByFlow(RulesByFlow.builder().audit(Audit.builder().build()).build());
        req.setActions(Actions.builder().build());
        req.setQuestions(List.of(Questions.builder().answerOptions(new ArrayList<>()).build()));
        req.setDetails(List.of(Details.builder().build()));
        Map<String, Object> eventMap = new HashMap<>();
        IQEResponse resp = new IQEResponse();

        when(rulesByFlowRepo.save(any())).thenReturn(Mono.just(new RulesByFlowEntity()));
        when(actionsRepo.save(any())).thenReturn(Mono.just(new ActionsEntity()));
        when(questionsRepo.save(any())).thenReturn(Mono.just(new QuestionsEntity()));
        when(answerOptionsRepo.save(any())).thenReturn(Mono.just(new AnswerOptionsEntity()));
        when(questionnaireDetailsRepo.save(any())).thenReturn(Mono.just(new QuestionsDetailsEntity()));
        when(redisCacheService.setDataToRedisRest(anyString(), anyString(), any())).thenReturn(Mono.justOrEmpty("success"));

        StepVerifier.create(orchestrator.insertQuestionsIntoDB(req, eventMap, resp))
                .verifyComplete();
    }*/

    @Test
    void assignSequenceIds_assignsToQuestionsAndOptions() {
        Questions q = Questions.builder()
                .answerOptions(List.of(AnswerOptions.builder().relatedQuestions(new ArrayList<>()).build()))
                .build();
        QuestionareRequest req = new QuestionareRequest();
        req.setQuestions(List.of(q));

        StepVerifier.create(orchestrator.assignSequenceIds(req))
                .expectNextMatches(r -> r.getQuestions().get(0).getSequenceId() == 1)
                .verifyComplete();
    }
}