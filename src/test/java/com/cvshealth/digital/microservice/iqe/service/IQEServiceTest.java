package com.cvshealth.digital.microservice.iqe.service;


import com.cvshealth.digital.microservice.iqe.dto.*;
import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
import com.cvshealth.digital.microservice.iqe.entity.QuestionnaireRules;

import com.cvshealth.digital.microservice.iqe.entity.*;
import com.cvshealth.digital.microservice.iqe.model.IQEResponse;
import com.cvshealth.digital.microservice.iqe.model.RulesDetails;
import com.cvshealth.digital.microservice.iqe.repository.*;
import com.cvshealth.digital.microservice.iqe.udt.AuditEntity;
import com.cvshealth.digital.microservice.iqe.utils.LoggingUtils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.kie.api.runtime.KieContainer;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.*;

import static graphql.Assert.assertNotNull;
import static graphql.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
@ExtendWith(MockitoExtension.class)
public class IQEServiceTest {

    @Mock
    private QuestionnaireRulesRepository questionnaireRulesRepository;

    @InjectMocks
    IQEService iqeService;

    @InjectMocks
    LoggingUtils loggingUtils;

    @Mock
    KieContainer kieContainer;




    /** The helper. */
    @Mock
    private IQERepoOrchestrator helper;

    /** The validator. */
    @Mock
    private RulesServiceRepoOrchestrator rulesServiceRepoOrchestrator;

    @Mock
    ActionsRepository actionsRepo;

    @Mock
    QuestionsRepository questionsRepo;

    @Mock
    RulesByFlowRepository rulesByFlowRepo;

    @Mock
    AnswerOptionsRepository answerOptionsRepo;

    @Mock
    QuestionnaireDetailsRepository questionnaireDetailsRepo;

    @Autowired
    private RedisCacheService redisCacheService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        iqeService = new IQEService(
                questionnaireRulesRepository, loggingUtils, helper,
                rulesServiceRepoOrchestrator, actionsRepo, questionsRepo,
                rulesByFlowRepo, answerOptionsRepo, redisCacheService, questionnaireDetailsRepo
        );
    }

    public Map<String, String> mockHttpHeaders()
    {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return headers;
    }

    @Test
   public void getRuleDetailsNoRules()   {
        List<QuestionnaireRules> questionnaireRulesList = new ArrayList<>();

        QuestionnaireRulesRepository   questionnaireRulesRepo = mock(QuestionnaireRulesRepository.class);

        Mockito.when(questionnaireRulesRepo.findByFlow(ArgumentMatchers.anyString())).thenReturn(Flux.fromIterable(questionnaireRulesList));

        RulesDetails rulesDetailsInput = new RulesDetails();
        rulesDetailsInput.setFlow("MC_CORE");
        rulesDetailsInput.setReasonId(86);
        rulesDetailsInput.setRequiredQuestionnaireContext("MC_CORE_ELIGIBILITY_QUESTION");


        IQEService iqeServiceClass = new IQEService(questionnaireRulesRepo, loggingUtils, helper,
                rulesServiceRepoOrchestrator, actionsRepo, questionsRepo, rulesByFlowRepo, answerOptionsRepo,redisCacheService,questionnaireDetailsRepo);

        Mono<com.cvshealth.digital.microservice.iqe.model.Questions> questionsMono = iqeServiceClass.getRuleDetails(rulesDetailsInput, mockHttpHeaders(), new HashMap<>());

        StepVerifier.create(questionsMono)
                .expectNextMatches(questions -> questions.getQuestions() == null)
                .expectComplete()
                .verify();

    }

    @Test
   public void getRuleDetailsQuestionaireRules()   {
        List<QuestionnaireRules> questionnaireRulesList = new ArrayList<>();

        createQuestionaireRulesList(questionnaireRulesList);

        QuestionnaireRulesRepository  questionnaireRulesRepo = mock(QuestionnaireRulesRepository.class);

        Mockito.when(questionnaireRulesRepo.findByFlow(ArgumentMatchers.anyString())).thenReturn(Flux.fromIterable(questionnaireRulesList));

        RulesDetails rulesDetailsInput = new RulesDetails();
        rulesDetailsInput.setFlow("MC_CORE");
        rulesDetailsInput.setReasonId(86);
        rulesDetailsInput.setRequiredQuestionnaireContext("MC_CORE_ELIGIBILITY_QUESTION");


        IQEService iqeServiceClass = new IQEService(questionnaireRulesRepo, loggingUtils, helper,
                rulesServiceRepoOrchestrator, actionsRepo, questionsRepo, rulesByFlowRepo, answerOptionsRepo,redisCacheService,questionnaireDetailsRepo);

        Mono<com.cvshealth.digital.microservice.iqe.model.Questions> questionsMono = iqeServiceClass.getRuleDetails(rulesDetailsInput, mockHttpHeaders(), new HashMap<>()).doOnNext(System.out::println);

        StepVerifier.create(questionsMono)
                .assertNext(questions -> {
                    Assert.assertNotNull(questions);
                    assertEquals("100000000051",questions.getQuestions().get(0).getId() );
                })
                .expectComplete()
                .verify();

    }


    @Test
    public void getRuleDetailsQuestionaireRulesNoRules()   {
        List<QuestionnaireRules> questionnaireRulesList = new ArrayList<>();

        createQuestionaireRulesList(questionnaireRulesList);

        QuestionnaireRulesRepository  questionnaireRulesRepo = mock(QuestionnaireRulesRepository.class);

        Mockito.when(questionnaireRulesRepo.findByFlow(ArgumentMatchers.anyString())).thenReturn(Flux.fromIterable(questionnaireRulesList));

        RulesDetails rulesDetailsInput = new RulesDetails();
        rulesDetailsInput.setFlow("MC_CORE");
        rulesDetailsInput.setReasonId(89);
        rulesDetailsInput.setRequiredQuestionnaireContext("MC_CORE_ELIGIBILITY_QUESTION");

        IQEService iqeServiceClass = new IQEService(questionnaireRulesRepo, loggingUtils, helper,
                rulesServiceRepoOrchestrator, actionsRepo, questionsRepo, rulesByFlowRepo, answerOptionsRepo,redisCacheService,questionnaireDetailsRepo);

        Mono<com.cvshealth.digital.microservice.iqe.model.Questions> questionsMono = iqeServiceClass.getRuleDetails(rulesDetailsInput, mockHttpHeaders(), new HashMap<>());

        StepVerifier.create(questionsMono)
                .expectNextMatches(questions -> questions.getQuestions() == null)
                .expectComplete()
                .verify();

    }

    @Test
    public void getRuleDetailsEligiblityNoRules()   {
        List<QuestionnaireRules> questionnaireRulesList = new ArrayList<>();

        QuestionnaireRulesRepository   questionnaireRulesRepo = mock(QuestionnaireRulesRepository.class);

        Mockito.when(questionnaireRulesRepo.findByFlow(ArgumentMatchers.anyString())).thenReturn(Flux.fromIterable(questionnaireRulesList));

        RulesDetails rulesDetailsInput = new RulesDetails();
        rulesDetailsInput.setFlow("ELIGIBILITY");
        rulesDetailsInput.setReasonId(89);
        rulesDetailsInput.setQuestionId("100000000051");
        rulesDetailsInput.setAnswerValue("Yes");


        IQEService iqeServiceClass = new IQEService(questionnaireRulesRepo, loggingUtils, helper,
                rulesServiceRepoOrchestrator, actionsRepo, questionsRepo, rulesByFlowRepo, answerOptionsRepo,redisCacheService,questionnaireDetailsRepo);

        Mono<com.cvshealth.digital.microservice.iqe.model.Questions> questionsMono = iqeServiceClass.getRuleDetails(rulesDetailsInput, mockHttpHeaders(), new HashMap<>());

        StepVerifier.create(questionsMono)
                .expectNextMatches(questions -> questions.getQuestions() == null)
                .expectComplete()
                .verify();
    }


    private static void createQuestionaireRulesList(List<QuestionnaireRules> questionnaireRulesList) {
        QuestionnaireRules questionnaireRules = new QuestionnaireRules();
        questionnaireRules.setCondition("requiredQuestionnaireContext == \"MC_CORE_ELIGIBILITY_QUESTION\" && reasonId==86");
        questionnaireRules.setFlow("MC_CORE");
        questionnaireRules.setRuleName("Wound_Care_MCCORE");
        questionnaireRules.setId("10009");
        questionnaireRules.setSalience(100);
        String action = "\"{\n   \\\"questions\\\": [\n      {\n        \\\"id\\\": \\\"100000000051\\\",\\n        \\\"text\\\": \\\"Do you have a cyst or boil on the skin? (required)\\\",\\n        \\\"answerType\\\": \\\"radio\\\",\\n        \\\"required\\\": true,\\n        \\\"answerOptions\\\": [\\n          {\\n            \\\"text\\\": \\\"Yes\\\",\\n            \\\"value\\\": \\\"1\\\"\\n          },\\n          {\\n            \\\"text\\\": \\\"No\\\",\\n            \\\"value\\\": \\\"2\\\"\\n          }\\n        ],\\n        \\\"services\\\": [\\n          {\\n            \\\"name\\\": \\\"Wound Care\\\",\\n            \\\"reasonId\\\": 86,\\n            \\\"reasonMappingId\\\": \\\"137\\\"\\n          }\\n        ]\\n      }\\n    ]\\n}\"";
        questionnaireRules.setAction(action);
        questionnaireRulesList.add(questionnaireRules);
    }


    @Test
    public void testGetAllRules() {

        List<RulesByFlowEntity> rulesByFlows = getRulesByFlow();

        RulesByFlowRepository rulesByFlowRepository = mock(RulesByFlowRepository.class);
        when(rulesByFlowRepository.findAll()).thenReturn(Flux.fromIterable(rulesByFlows));

        IQEService iqeServiceClass = new IQEService( questionnaireRulesRepository, loggingUtils, helper,
                rulesServiceRepoOrchestrator, actionsRepo, questionsRepo, rulesByFlowRepository, answerOptionsRepo,redisCacheService,questionnaireDetailsRepo);

        Mono<QuestionareRequest> result = iqeServiceClass.rules();

        StepVerifier.create(result)
                .expectNextMatches(request -> {
                    List<RulesByFlowEntity> activeRules = request.getActiveRules();
                    return activeRules.size() > 1;
                })
                .expectComplete()
                .verify();
    }


    @Test
    public void testGetQuestionareByActionId() {
        String actionId = "131c1c73-3f2a-4f7d-9252-c90467f9e525";
        QuestionareRequest questionareRequest = new QuestionareRequest();

        RulesByFlowEntity rulesByFlow = setupRulesByFlow();
        ActionsEntity actions = setupActions();
        List<QuestionsEntity> questions = setupQuestions();
        List<AnswerOptionsEntity> answerOptions = setupAnswerOptions();

        RulesByFlowRepository rulesByFlowRepository = mock(RulesByFlowRepository.class);
        QuestionsRepository questionsRepository = mock(QuestionsRepository.class);
        ActionsRepository actionsRepository = mock(ActionsRepository.class);
        AnswerOptionsRepository answerOptionsRepository = mock(AnswerOptionsRepository.class);
        RedisCacheService redisCachingService=mock(RedisCacheService.class);

        when(rulesByFlowRepository.findByActionId(actionId)).thenReturn(Flux.just(rulesByFlow));
        when(actionsRepository.findByActionId(actionId)).thenReturn(Flux.just(actions));
        when(questionsRepository.findByActionId(actionId)).thenReturn(Flux.fromIterable(questions));
        when(answerOptionsRepository.findByActionId(actionId)).thenReturn(Flux.fromIterable(answerOptions));
        when(redisCachingService.getDataFromRedis(Mockito.anyString(),Mockito.anyString(),Mockito.anyMap())).thenReturn(Mono.empty());



        IQEService iqeServiceClass = new IQEService( questionnaireRulesRepository, loggingUtils, helper,
                rulesServiceRepoOrchestrator, actionsRepository, questionsRepository, rulesByFlowRepository, answerOptionsRepository,redisCachingService,questionnaireDetailsRepo);

        Mono<QuestionareRequest> result = iqeServiceClass.questionnaireByActionId(actionId, questionareRequest);

        StepVerifier.create(result)
                .expectNextMatches(request -> {
                    return request.getRulesByFlow().getFlow().equals("flow1.5");
                })
                .verifyComplete();
    }

    @Test
    public void testGetQuestionareByActionId_EmptyValues() {
        String actionId = "131c1c73-3f2a-4f7d-9252-c90467f9e5251";
        QuestionareRequest questionareRequest = new QuestionareRequest();

        RulesByFlowEntity rulesByFlow = new RulesByFlowEntity();
        ActionsEntity actions = new ActionsEntity();
        List<QuestionsEntity> questions = new ArrayList<>();
        List<AnswerOptionsEntity> answerOptions = new ArrayList<>();

        RulesByFlowRepository rulesByFlowRepository = mock(RulesByFlowRepository.class);
        QuestionsRepository questionsRepository = mock(QuestionsRepository.class);
        ActionsRepository actionsRepository = mock(ActionsRepository.class);
        AnswerOptionsRepository answerOptionsRepository = mock(AnswerOptionsRepository.class);
        RedisCacheService redisCachingService=mock(RedisCacheService.class);

        when(redisCachingService.getDataFromRedis(Mockito.anyString(),Mockito.anyString(),Mockito.anyMap())).thenReturn(Mono.empty());
        when(rulesByFlowRepository.findByActionId(actionId)).thenReturn(Flux.just(rulesByFlow));
        when(actionsRepository.findByActionId(actionId)).thenReturn(Flux.just(actions));
        when(questionsRepository.findByActionId(actionId)).thenReturn(Flux.fromIterable(questions));
        when(answerOptionsRepository.findByActionId(actionId)).thenReturn(Flux.fromIterable(answerOptions));

        IQEService iqeServiceClass = new IQEService( questionnaireRulesRepository, loggingUtils, helper,
                rulesServiceRepoOrchestrator, actionsRepository, questionsRepository, rulesByFlowRepository, answerOptionsRepository,redisCachingService,questionnaireDetailsRepo);

        Mono<QuestionareRequest> result = iqeServiceClass.questionnaireByActionId(actionId, questionareRequest);

        StepVerifier.create(result)
                .expectNextMatches(request -> {
                    return request.getStatusCode().equalsIgnoreCase("5008");
                })
                .verifyComplete();
    }


    @Test
    public void testProcessAndStoreInputData() {
        QuestionareRequest questionareRequest = new QuestionareRequest();
        questionareRequest.setRulesByFlow(RulesByFlow.builder()
                .flow("flow1.5")
                .ruleName("rule1")
                .condition("condition5")
                .lob("lob1")
                .salience(1)
                .build());
        questionareRequest.setActions(Actions.builder()
                .actionText("action2")
                .build());
        questionareRequest.setQuestions(Collections.singletonList(Questions.builder()
                .text("question1")
                .errorMessage("Please enter valid question")
                .answerType("radio").sequenceId(1)
                .answerOptions(Collections.singletonList(AnswerOptions.builder()
                        .text("answer1")
                        .value("hi").sequenceId(1)
                        .build()))
                .build()));

        Map<String, String> reqHdrMap = new HashMap<>();
        reqHdrMap.put("header1", "value1");

        IQEResponse iqeResponse = new IQEResponse();

        Map<String, Object> eventMap = new HashMap<>();

        RulesServiceRepoOrchestrator rulesServiceRepo = mock(RulesServiceRepoOrchestrator.class);

        when(rulesServiceRepo.validateRequest(questionareRequest, iqeResponse)).thenReturn(Mono.just(questionareRequest));

        IQERepoOrchestrator helperClass = mock(IQERepoOrchestrator.class);
        QuestionsRepository questionsRepository = mock(QuestionsRepository.class);
        ActionsRepository actionsRepository = mock(ActionsRepository.class);
        AnswerOptionsRepository answerOptionsRepository = mock(AnswerOptionsRepository.class);

        when(helperClass.assignSequenceIds(questionareRequest)).thenReturn(Mono.just(questionareRequest));
        when(helperClass.processInputData(questionareRequest, reqHdrMap, iqeResponse, eventMap)).thenReturn(Mono.just(questionareRequest));
        when(helperClass.insertQuestionsIntoDB(questionareRequest, eventMap, iqeResponse)).thenAnswer(invocation -> {
            iqeResponse.setStatusCode("0000");
            iqeResponse.setStatusDescription("Data inserted successfully");
            return Mono.just(iqeResponse);
        });

        IQEService iqeServiceClass = new IQEService( questionnaireRulesRepository, loggingUtils, helperClass,
                rulesServiceRepo, actionsRepository, questionsRepository, rulesByFlowRepo, answerOptionsRepository,redisCacheService,questionnaireDetailsRepo);

        Mono<IQEResponse> result = iqeServiceClass.processQuestionnaire(questionareRequest,iqeResponse, reqHdrMap , eventMap);

        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    return response.getStatusCode().equals("0000")
                            && response.getStatusDescription().equals("Data inserted successfully");
                })
                .verifyComplete();
    }

    @Test
    public void testDeleteQuestionareByActionId_ActionIdFound() {

        String actionId = "748b1a95-87a4-400a-ac5b-97330699d4d2";
        String ruleId = "97e60d8c-656a-40c5-afd9-04a117a40ff5";
        String flow = "rule1";


        RulesByFlowEntity rulesByFlow = new RulesByFlowEntity();
        rulesByFlow.setActionId(actionId);
        rulesByFlow.setRuleId(ruleId);
        rulesByFlow.setFlow(flow);

        RulesByFlowRepository rulesByFlowRepository = mock(RulesByFlowRepository.class);
        QuestionsRepository questionsRepository = mock(QuestionsRepository.class);
        ActionsRepository actionsRepository = mock(ActionsRepository.class);
        AnswerOptionsRepository answerOptionsRepository = mock(AnswerOptionsRepository.class);
        QuestionnaireDetailsRepository questionnaireDetailsRepository = mock(QuestionnaireDetailsRepository.class);
        RedisCacheService redisCacheServiceClass = mock(RedisCacheService.class);

        when(rulesByFlowRepository.findByActionId(actionId)).thenReturn(Flux.just(rulesByFlow));
        when(rulesByFlowRepository.deleteByFlowAndRuleId(flow, ruleId)).thenReturn(Mono.empty());
        when(actionsRepository.deleteByActionId(actionId)).thenReturn(Mono.empty());
        when(questionsRepository.deleteByActionId(actionId)).thenReturn(Mono.empty());
        when(answerOptionsRepository.deleteByActionId(actionId)).thenReturn(Mono.empty());
        when(questionnaireDetailsRepository.deleteByActionId(actionId)).thenReturn(Mono.empty());
        when(redisCacheServiceClass.deleteDataFromRedis(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap())).thenReturn(Mono.empty());

        IQEService iqeServiceClass = new IQEService( questionnaireRulesRepository, loggingUtils, helper,
                rulesServiceRepoOrchestrator, actionsRepository, questionsRepository, rulesByFlowRepository, answerOptionsRepository,redisCacheServiceClass,questionnaireDetailsRepository);

        Mono<IQEResponse> result = iqeServiceClass.deleteQuestionnaireByActionId(actionId);


        StepVerifier.create(result)
                .expectNextMatches(iqeResponse -> iqeResponse.getStatusCode().equals("0000") && iqeResponse.getStatusDescription().equals("ActionId deleted successfully"))
                .verifyComplete();
    }

    @Test
    public void testDeleteQuestionareByActionId_ActionIdNotFound() {

        String actionId = "748b1a95-87a4-400a-ac5b-97330699d4d2";

        RulesByFlowRepository rulesByFlowRepository = mock(RulesByFlowRepository.class);

        when(rulesByFlowRepository.findByActionId(actionId)).thenReturn(Flux.empty());

        IQEService iqeServiceClass = new IQEService(questionnaireRulesRepository, loggingUtils, helper,
                rulesServiceRepoOrchestrator, actionsRepo, questionsRepo, rulesByFlowRepository, answerOptionsRepo,redisCacheService,questionnaireDetailsRepo);

        Mono<IQEResponse> result = iqeServiceClass.deleteQuestionnaireByActionId(actionId);

        StepVerifier.create(result)
                .expectNextMatches(iqeResponse -> iqeResponse.getStatusCode().equals("5009") &&
                        iqeResponse.getActionId().equals("ActionId not found"))
                .verifyComplete();
    }


    @Test
    public void testQuestionnaireByFlowAndCondition() {

        // Arrange
        RulesDetails rulesDetails = new RulesDetails();
        rulesDetails.setFlow("flow1.49");
        rulesDetails.setActionId("1928d95f-3eaf-432b-a771-52442f6778e9");

        List<RulesByFlowEntity> rulesByFlowList = new ArrayList<>();

        RulesByFlowEntity rulesByFlow = RulesByFlowEntity.builder()
                .flow("flow1.49")
                .ruleId("f146046e-fd6d-481c-bcf6-95aa17ec6d6e")
                .ruleName("rule1")
                .actionId("1928d95f-3eaf-432b-a771-52442f6778e9")
                .condition("requiredQuestionnaireContext==\"condition6\"")
                .lob("lob1")
                .salience(1)
                .isActive(true)
                .build();

        rulesByFlowList.add(rulesByFlow);

        Map<String, String> reqHdrMap = new HashMap<>();
        reqHdrMap.put("header1", "value1");

        RulesByFlowRepository rulesByFlowRepository = mock(RulesByFlowRepository.class);
        QuestionsRepository questionsRepository = mock(QuestionsRepository.class);
        ActionsRepository actionsRepository = mock(ActionsRepository.class);
        AnswerOptionsRepository answerOptionsRepository = mock(AnswerOptionsRepository.class);
        RedisCacheService redisCachingService=mock(RedisCacheService.class);

        when(redisCachingService.getDataFromRedis(Mockito.anyString(),Mockito.anyString(),Mockito.anyMap())).thenReturn(Mono.empty());
        Mockito.when(rulesByFlowRepository.findByFlow(rulesDetails.getFlow())).thenReturn(Flux.fromIterable(rulesByFlowList));

        Mockito.when(rulesByFlowRepository.findByActionId(Mockito.anyString())).thenReturn(Flux.empty());

        Mockito.when(actionsRepository.findByActionId(Mockito.anyString())).thenReturn(Flux.empty());

        QuestionareRequest expectedOutput = setQuestionareRequestData();

        IQEService iqeServiceClass = new IQEService( questionnaireRulesRepository, loggingUtils, helper,
                rulesServiceRepoOrchestrator, actionsRepository, questionsRepository, rulesByFlowRepository, answerOptionsRepository,redisCachingService,questionnaireDetailsRepo);

        Mono<QuestionareRequest> result = iqeServiceClass.questionnaireByFlowAndCondition(rulesDetails, expectedOutput, reqHdrMap);

        StepVerifier.create(result)
                .expectNextMatches(iqeResponse -> iqeResponse.getRulesByFlow().getActionId().equals("1928d95f-3eaf-432b-a771-52442f6778e9")
                       )
                .verifyComplete();

    }


    @Test
    public void testQuestionnaireByFlowAndCondition_inValidData() {

        RulesDetails rulesDetails = new RulesDetails();
        rulesDetails.setFlow("flow1.49");
        rulesDetails.setActionId("1928d95f-3eaf-432b-a771-52442f6778e9");

        List<RulesByFlowEntity> rulesByFlowList = new ArrayList<>();

        RulesByFlowEntity rulesByFlow = RulesByFlowEntity.builder()
                .flow("flow1.49")
                .ruleId("f146046e-fd6d-481c-bcf6-95aa17ec6d6e")
                .ruleName("rule1")
                .actionId("1928d95f-3eaf-432b-a771-52442f6778e9")
                .condition("requiredQuestionnaireContext==\"condition6\"")
                .lob("lob1")
                .salience(1)
                .isActive(true)
                .build();

        rulesByFlowList.add(rulesByFlow);

        Map<String, String> reqHdrMap = new HashMap<>();
        reqHdrMap.put("header1", "value1");

        RulesByFlowRepository rulesByFlowRepository = mock(RulesByFlowRepository.class);
        QuestionsRepository questionsRepository = mock(QuestionsRepository.class);
        ActionsRepository actionsRepository = mock(ActionsRepository.class);
        AnswerOptionsRepository answerOptionsRepository = mock(AnswerOptionsRepository.class);
        RedisCacheService redisCachingService=mock(RedisCacheService.class);

        when(redisCachingService.getDataFromRedis(Mockito.anyString(),Mockito.anyString(),Mockito.anyMap())).thenReturn(Mono.empty());

        Mockito.when(rulesByFlowRepository.findByFlow(rulesDetails.getFlow())).thenReturn(Flux.fromIterable(rulesByFlowList));

        Mockito.when(rulesByFlowRepository.findByActionId(Mockito.anyString())).thenReturn(Flux.empty());

        Mockito.when(actionsRepository.findByActionId(Mockito.anyString())).thenReturn(Flux.empty());

        QuestionareRequest expectedOutput = new QuestionareRequest();

        IQEService iqeServiceClass = new IQEService( questionnaireRulesRepository, loggingUtils, helper,
                rulesServiceRepoOrchestrator, actionsRepository, questionsRepository, rulesByFlowRepository, answerOptionsRepository,redisCachingService,questionnaireDetailsRepo);

        Mono<QuestionareRequest> result = iqeServiceClass.questionnaireByFlowAndCondition(rulesDetails, expectedOutput, reqHdrMap);

        StepVerifier.create(result)
                .expectNextMatches(iqeResponse -> iqeResponse.getRulesByFlow()==null)
                .verifyComplete();

    }

    @Test
    public void testQuestionnaireByActionAndQuestionId() {



        String actionId="1928d95f-3eaf-432b-a771-52442f6778e9";
        String questionId="48a14bb6-6a27-45e2-9428-95d738d56990";

        QuestionareRequest expectedOutput = setQuestionareRequestData();

        Questions questions = expectedOutput.getQuestions().get(0);


        QuestionsEntity questionEntity = QuestionsEntity.builder()
                .actionId(questions.getActionId())
                .questionId(questions.getQuestionId())
                .questionText(questions.getText())
                .answerType(questions.getAnswerType())
                .answerOptionId(questions.getAnswerOptionIds())
                .build();

        List<AnswerOptions> answerOptions = questions.getAnswerOptions();

        List<AnswerOptionsEntity> answerOptionsEntities = answerOptions.stream()
                .map(answerOption -> {
                    return AnswerOptionsEntity.builder()
                            .answerOptionId(answerOption.getAnswerOptionId())
                            .answerText(answerOption.getText())
                            .answerValue(answerOption.getValue())
                            .actionId(answerOption.getActionId())
                            .questionId(answerOption.getQuestionId())
                            .sequence_id(answerOption.getSequenceId())
                            .build();
                })
                .toList();

        Map<String, String> reqHdrMap = new HashMap<>();
        reqHdrMap.put("header1", "value1");

        RulesByFlowRepository rulesByFlowRepository = mock(RulesByFlowRepository.class);
        QuestionsRepository questionsRepository = mock(QuestionsRepository.class);
        ActionsRepository actionsRepository = mock(ActionsRepository.class);
        AnswerOptionsRepository answerOptionsRepository = mock(AnswerOptionsRepository.class);

        Mockito.when(questionsRepository.findByActionIdAndQuestionId(actionId,questionId)).thenReturn(Mono.just(questionEntity));

        Mockito.when( answerOptionsRepository.findByActionIdAndQuestionId(actionId, questionId)).thenReturn(Flux.fromIterable(answerOptionsEntities));

        IQEService iqeServiceClass = new IQEService( questionnaireRulesRepository, loggingUtils, helper,
                rulesServiceRepoOrchestrator, actionsRepository, questionsRepository, rulesByFlowRepository, answerOptionsRepository,redisCacheService,questionnaireDetailsRepo);

        Mono<QuestionareRequest> result = iqeServiceClass.questionnaireByActionAndQuestionId(actionId, questionId, reqHdrMap);

        StepVerifier.create(result)
                .expectNextMatches(iqeResponse -> iqeResponse.getQuestion().getActionId().equals("1928d95f-3eaf-432b-a771-52442f6778e9"))
                .verifyComplete();

    }

    @Test
    public void testQuestionnaireByActionAndQuestionId_inValidData() {



        String actionId="1928d95f-3eaf-432b-a771-52442f6778e9";
        String questionId="48a14bb6-6a27-45e2-9428-95d738d56990";

        Map<String, String> reqHdrMap = new HashMap<>();
        reqHdrMap.put("header1", "value1");

        RulesByFlowRepository rulesByFlowRepository = mock(RulesByFlowRepository.class);
        QuestionsRepository questionsRepository = mock(QuestionsRepository.class);
        ActionsRepository actionsRepository = mock(ActionsRepository.class);
        AnswerOptionsRepository answerOptionsRepository = mock(AnswerOptionsRepository.class);

        Mockito.when(questionsRepository.findByActionIdAndQuestionId(actionId,questionId)).thenReturn(Mono.empty());

        Mockito.when( answerOptionsRepository.findByActionIdAndQuestionId(actionId, questionId)).thenReturn(Flux.empty());

        IQEService iqeServiceClass = new IQEService( questionnaireRulesRepository, loggingUtils, helper,
                rulesServiceRepoOrchestrator, actionsRepository, questionsRepository, rulesByFlowRepository, answerOptionsRepository,redisCacheService,questionnaireDetailsRepo);

        Mono<QuestionareRequest> result = iqeServiceClass.questionnaireByActionAndQuestionId(actionId, questionId, reqHdrMap);

        StepVerifier.create(result)
                .expectComplete()
                .verify();
    }



    @Test
    public void testQuestionnaireByFlow_validData() {

        RulesDetails rulesDetails = new RulesDetails();
        rulesDetails.setFlow("flow1.49");
        rulesDetails.setActionId("1928d95f-3eaf-432b-a771-52442f6778e9");
        rulesDetails.setQuestionId("48a14bb6-6a27-45e2-9428-95d738d56990");

        List<RulesByFlowEntity> rulesByFlowList = new ArrayList<>();

        RulesByFlowEntity rulesByFlow = RulesByFlowEntity.builder()
                .flow("flow1.49")
                .ruleId("f146046e-fd6d-481c-bcf6-95aa17ec6d6e")
                .ruleName("rule1")
                .actionId("1928d95f-3eaf-432b-a771-52442f6778e9")
                .condition("requiredQuestionnaireContext==\"condition6\"")
                .lob("lob1")
                .salience(1)
                .isActive(true)
                .build();

        rulesByFlowList.add(rulesByFlow);

        QuestionareRequest expectedOutput = setQuestionareRequestData();

        Questions questions = expectedOutput.getQuestions().get(0);

        expectedOutput.setQuestion(questions);

        Map<String, String> reqHdrMap = new HashMap<>();
        reqHdrMap.put("header1", "value1");

        RulesByFlowRepository rulesByFlowRepository = mock(RulesByFlowRepository.class);
        QuestionsRepository questionsRepository = mock(QuestionsRepository.class);
        ActionsRepository actionsRepository = mock(ActionsRepository.class);
        AnswerOptionsRepository answerOptionsRepository = mock(AnswerOptionsRepository.class);

        Mockito.when(rulesByFlowRepository.findByFlow(rulesDetails.getFlow())).thenReturn(Flux.fromIterable(rulesByFlowList));

        Mockito.when(actionsRepository.findByActionId(Mockito.anyString())).thenReturn(Flux.empty());

        IQEService iqeServiceClass = new IQEService( questionnaireRulesRepository, loggingUtils, helper,
                rulesServiceRepoOrchestrator, actionsRepository, questionsRepository, rulesByFlowRepository, answerOptionsRepository,redisCacheService,questionnaireDetailsRepo);

        Mono<QuestionareRequest> result = iqeServiceClass.questionnaireByFlow(rulesDetails, expectedOutput, reqHdrMap);

        StepVerifier.create(result)
                .expectNextMatches(iqeResponse -> iqeResponse.getQuestion().getActionId().equals("1928d95f-3eaf-432b-a771-52442f6778e9"))
                .verifyComplete();

    }


    @Test
    public void testQuestionnaireByFlow_InValidData() {

        RulesDetails rulesDetails = new RulesDetails();
        rulesDetails.setFlow("flow1.49");
        rulesDetails.setActionId("1928d95f-3eaf-432b-a771-52442f6778e9");
        rulesDetails.setQuestionId("48a14bb6-6a27-45e2-9428-95d738d56990");

        List<RulesByFlowEntity> rulesByFlowList = new ArrayList<>();

        RulesByFlowEntity rulesByFlow = RulesByFlowEntity.builder()
                .flow("flow1.49")
                .ruleId("f146046e-fd6d-481c-bcf6-95aa17ec6d6e")
                .ruleName("rule1")
                .actionId("1928d95f-3eaf-432b-a771-52442f6778e9")
                .condition("requiredQuestionnaireContext==\"condition6\"")
                .lob("lob1")
                .salience(1)
                .isActive(true)
                .build();

        rulesByFlowList.add(rulesByFlow);

        QuestionareRequest expectedOutput = new QuestionareRequest();

        Map<String, String> reqHdrMap = new HashMap<>();
        reqHdrMap.put("header1", "value1");

        RulesByFlowRepository rulesByFlowRepository = mock(RulesByFlowRepository.class);
        QuestionsRepository questionsRepository = mock(QuestionsRepository.class);
        ActionsRepository actionsRepository = mock(ActionsRepository.class);
        AnswerOptionsRepository answerOptionsRepository = mock(AnswerOptionsRepository.class);

        Mockito.when(rulesByFlowRepository.findByFlow(rulesDetails.getFlow())).thenReturn(Flux.empty());

        Mockito.when(actionsRepository.findByActionId(Mockito.anyString())).thenReturn(Flux.empty());

        IQEService iqeServiceClass = new IQEService( questionnaireRulesRepository, loggingUtils, helper,
                rulesServiceRepoOrchestrator, actionsRepository, questionsRepository, rulesByFlowRepository, answerOptionsRepository,redisCacheService,questionnaireDetailsRepo);

        Mono<QuestionareRequest> result = iqeServiceClass.questionnaireByFlow(rulesDetails, expectedOutput, reqHdrMap);

        StepVerifier.create(result)
                .expectNextMatches(iqeResponse -> iqeResponse.getQuestion()==null)
                .verifyComplete();

    }

    private List<RulesByFlowEntity> getRulesByFlow() {
        RulesByFlowEntity activeRule1 = RulesByFlowEntity.builder()
                .flow("flow1.4")
                .ruleId("97e60d8c-656a-40c5-afd9-04a117a40ff5")
                .ruleName("rule1")
                .actionId("50eefa18-cd7c-47dd-9de4-d3374b3f5d6b")
                .condition("condition5")
                .lob("lob1")
                .salience(1)
                .audit(AuditEntity.builder()
                        .createdTs("2024-12-03 20:49:17.164")
                        .createdBy("Default user")
                        .build())
                .isActive(true)
                .build();

        RulesByFlowEntity activeRule2 = RulesByFlowEntity.builder()
                .flow("flow1.4")
                .ruleId("9b370479-774a-499a-a6a3-0fc28aadc46b")
                .ruleName("rule1")
                .actionId("c62c574f-df28-417f-9f5d-39984ab3829b")
                .condition("condition4")
                .lob("lob1")
                .salience(1)
                .audit(AuditEntity.builder()
                        .createdTs("2024-12-03 20:49:06.746")
                        .createdBy("Default user")
                        .build())
                .isActive(true)
                .build();

        return Arrays.asList(activeRule1, activeRule2);
    }

    private RulesByFlowEntity setupRulesByFlow() {
        RulesByFlowEntity rulesByFlow = new RulesByFlowEntity();
        rulesByFlow.setFlow("flow1.5");
        rulesByFlow.setRuleId("748b1a95-87a4-400a-ac5b-97330699d4d2");
        rulesByFlow.setRuleName("rule1");
        rulesByFlow.setActionId("131c1c73-3f2a-4f7d-9252-c90467f9e525");
        rulesByFlow.setCondition("condition5");
        rulesByFlow.setLob("lob1");
        rulesByFlow.setSalience(1);
        rulesByFlow.setActive(true);

        AuditEntity auditData = new AuditEntity();
        auditData.setCreatedTs("2024-12-05 14:59:03.817");
        auditData.setCreatedBy("Default user");
        rulesByFlow.setAudit(auditData);

        return rulesByFlow;
    }

    private ActionsEntity setupActions() {
        ActionsEntity actions = new ActionsEntity();
        actions.setActionId("131c1c73-3f2a-4f7d-9252-c90467f9e525");
        actions.setActionText("action2");
        actions.setQuestionId(List.of("763944ca-9d67-4096-a9ca-32bb7ee17490"));
        return actions;
    }

    private List<QuestionsEntity> setupQuestions() {
        QuestionsEntity question = new QuestionsEntity();
        question.setActionId("131c1c73-3f2a-4f7d-9252-c90467f9e525");
        question.setQuestionId("763944ca-9d67-4096-a9ca-32bb7ee17490");
        question.setQuestionText("question1");
        question.setErrorMessage("Please enter valid question");
        question.setAnswerType("radio");
        question.setAnswerOptionId(List.of("339d7fb2-d8fb-4184-a697-19834376e6ea"));
        question.setHelpText("");
        question.setCharacterLimit(0);
        question.setStacked(false);
        question.setSequence_id(1);

        return List.of(question);
    }

    private List<QuestionsEntity> setupQuestionsNested() {
        List<QuestionsEntity> questions = new ArrayList<>();

        // 1. Are you experiencing symptoms?
        questions.add(QuestionsEntity.builder()
                .questionId("RX#533")
                .questionText("Are you experiencing symptoms?")
                .answerType("radio")
                .required(true)
                .stacked(true)
                .errorMessage("Select whether you are experiencing symptoms")
                .helpText("What are severe symptoms?")
                .answerOptionId(Arrays.asList("RX#533_1", "RX#533_2", "RX#533_3"))
                .build()
        );

        // 2. Recent COVID-19 exposure
        questions.add(QuestionsEntity.builder()
                .questionId("CVS#760")
                .questionText("Do you have any recent exposure to someone with confirmed COVID-19?")
                .answerType("radio")
                .required(true)
                .stacked(false)
                .errorMessage("Select whether you've had recent exposure")
                .answerOptionId(Arrays.asList("CVS#760_1", "CVS#760_0"))
                .build()
        );

        // 3. Health insurance with related question
        questions.add(QuestionsEntity.builder()
                .questionId("TTP#1")
                .questionText("What type of health insurance do you have?")
                .answerType("radio")
                .required(true)
                .stacked(true)
                .errorMessage("Select what type of health insurance you have")
                .answerOptionId(Arrays.asList("TTP#1_1", "TTP#1_2", "TTP#1_3", "TTP#1_4"))
                .build()
        );
        // Related question for private insurance
        questions.add(QuestionsEntity.builder()
                .questionId("TTP#3")
                .questionText("Do you have Aetna insurance?")
                .answerType("radio")
                .required(true)
                .stacked(false)
                .errorMessage("Select whether you have Aetna insurance")
                .answerOptionId(Arrays.asList("TTP#3_1", "TTP#3_0"))
                .build()
        );

        // 4. U.S. veteran
        questions.add(QuestionsEntity.builder()
                .questionId("CVS#2485")
                .questionText("Are you a U.S veteran?")
                .answerType("radio")
                .required(true)
                .stacked(true)
                .errorMessage("Select whether you are a U.S veteran")
                .answerOptionId(Arrays.asList("CVS#2485_1", "CVS#2485_2"))
                .build()
        );

        // 5. Sex assigned at birth with related questions
        questions.add(QuestionsEntity.builder()
                .questionId("TTP#2")
                .questionText("What's your sex assigned at birth?")
                .answerType("radio")
                .required(true)
                .stacked(true)
                .errorMessage("Select your sex assigned at birth")
                .helpText("Why do we ask for sex assigned at birth?")
                .characterLimit(0)
                .answerOptionId(Arrays.asList("TTP#2_1", "TTP#2_2"))
                .build()
        );
        // Related questions for Female
        questions.add(QuestionsEntity.builder()
                .questionId("RX#540")
                .questionText("Are you pregnant?")
                .answerType("radio")
                .required(true)
                .stacked(false)
                .errorMessage("Select whether you are pregnant")
                .answerOptionId(Arrays.asList("RX#540_1", "RX#540_0"))
                .build()
        );
        questions.add(QuestionsEntity.builder()
                .questionId("RX#541")
                .questionText("Have you given birth in the past 2 weeks?")
                .answerType("radio")
                .required(true)
                .stacked(false)
                .errorMessage("Select whether you have given birth in the past 2 weeks")
                .answerOptionId(Arrays.asList("RX#541_1", "RX#541_0"))
                .build()
        );
        questions.add(QuestionsEntity.builder()
                .questionId("RX#542")
                .questionText("Are you breastfeeding?")
                .answerType("radio")
                .required(true)
                .stacked(false)
                .errorMessage("Select whether you are breastfeeding")
                .answerOptionId(Arrays.asList("RX#542_1", "RX#542_0"))
                .build()
        );

        return questions;
    }

    private List<AnswerOptionsEntity> setupAnswerOptionsNested() {
        List<AnswerOptionsEntity> answerOptions = new ArrayList<>();

        // RX#533: Are you experiencing symptoms?
        answerOptions.add(AnswerOptionsEntity.builder()
                .questionId("RX#533")
                .answerOptionId("RX#533_1")
                .answerText("Yes, mild to moderate")
                .answerValue("1")
                .build());
        answerOptions.add(AnswerOptionsEntity.builder()
                .questionId("RX#533")
                .answerOptionId("RX#533_2")
                .answerText("Yes, severe")
                .answerValue("2")
                .build());
        answerOptions.add(AnswerOptionsEntity.builder()
                .questionId("RX#533")
                .answerOptionId("RX#533_3")
                .answerText("No")
                .answerValue("3")
                .build());

        // CVS#760: Recent COVID-19 exposure
        answerOptions.add(AnswerOptionsEntity.builder()
                .questionId("CVS#760")
                .answerOptionId("CVS#760_1")
                .answerText("Yes")
                .answerValue("1")
                .build());
        answerOptions.add(AnswerOptionsEntity.builder()
                .questionId("CVS#760")
                .answerOptionId("CVS#760_0")
                .answerText("No")
                .answerValue("0")
                .build());

        // TTP#1: Health insurance
        answerOptions.add(AnswerOptionsEntity.builder()
                .questionId("TTP#1")
                .answerOptionId("TTP#1_1")
                .answerText("I have private insurance")
                .answerValue("1")
                .relatedQuestions(List.of("TTP#3"))
                .build());
        answerOptions.add(AnswerOptionsEntity.builder()
                .questionId("TTP#1")
                .answerOptionId("TTP#1_2")
                .answerText("I have Medicare or Tricare")
                .answerValue("2")
                .build());
        answerOptions.add(AnswerOptionsEntity.builder()
                .questionId("TTP#1")
                .answerOptionId("TTP#1_3")
                .answerText("I have Medicaid or other government-funded insurance")
                .answerValue("3")
                .build());
        answerOptions.add(AnswerOptionsEntity.builder()
                .questionId("TTP#1")
                .answerOptionId("TTP#1_4")
                .answerText("I'm uninsured")
                .answerValue("4")
                .build());

        // TTP#3: Do you have Aetna insurance?
        answerOptions.add(AnswerOptionsEntity.builder()
                .questionId("TTP#3")
                .answerOptionId("TTP#3_1")
                .answerText("Yes")
                .answerValue("1")
                .build());
        answerOptions.add(AnswerOptionsEntity.builder()
                .questionId("TTP#3")
                .answerOptionId("TTP#3_0")
                .answerText("No")
                .answerValue("0")
                .build());

        // CVS#2485: Are you a U.S veteran?
        answerOptions.add(AnswerOptionsEntity.builder()
                .questionId("CVS#2485")
                .answerOptionId("CVS#2485_1")
                .answerText("I am a U.S. veteran")
                .answerValue("1")
                .build());
        answerOptions.add(AnswerOptionsEntity.builder()
                .questionId("CVS#2485")
                .answerOptionId("CVS#2485_2")
                .answerText("I am not a U.S. veteran")
                .answerValue("2")
                .build());

        // TTP#2: Sex assigned at birth
        answerOptions.add(AnswerOptionsEntity.builder()
                .questionId("TTP#2")
                .answerOptionId("TTP#2_1")
                .answerText("Female")
                .answerValue("1")
                .relatedQuestions(List.of("RX#540", "RX#541", "RX#542"))
                .build());
        answerOptions.add(AnswerOptionsEntity.builder()
                .questionId("TTP#2")
                .answerOptionId("TTP#2_2")
                .answerText("Male")
                .answerValue("2")
                .build());

        // RX#540: Are you pregnant?
        answerOptions.add(AnswerOptionsEntity.builder()
                .questionId("RX#540")
                .answerOptionId("RX#540_1")
                .answerText("Yes")
                .answerValue("1")
                .build());
        answerOptions.add(AnswerOptionsEntity.builder()
                .questionId("RX#540")
                .answerOptionId("RX#540_0")
                .answerText("No")
                .answerValue("0")
                .build());

        // RX#541: Have you given birth in the past 2 weeks?
        answerOptions.add(AnswerOptionsEntity.builder()
                .questionId("RX#541")
                .answerOptionId("RX#541_1")
                .answerText("Yes")
                .answerValue("1")
                .build());
        answerOptions.add(AnswerOptionsEntity.builder()
                .questionId("RX#541")
                .answerOptionId("RX#541_0")
                .answerText("No")
                .answerValue("0")
                .build());

        // RX#542: Are you breastfeeding?
        answerOptions.add(AnswerOptionsEntity.builder()
                .questionId("RX#542")
                .answerOptionId("RX#542_1")
                .answerText("Yes")
                .answerValue("1")
                .build());
        answerOptions.add(AnswerOptionsEntity.builder()
                .questionId("RX#542")
                .answerOptionId("RX#542_0")
                .answerText("No")
                .answerValue("0")
                .build());

        return answerOptions;
    }

    private List<AnswerOptionsEntity> setupAnswerOptions() {
        AnswerOptionsEntity answerOption = new AnswerOptionsEntity();
        answerOption.setActionId("131c1c73-3f2a-4f7d-9252-c90467f9e525");
        answerOption.setQuestionId("763944ca-9d67-4096-a9ca-32bb7ee17490");
        answerOption.setAnswerOptionId("339d7fb2-d8fb-4184-a697-19834376e6ea");
        answerOption.setAnswerText("answer1");
        answerOption.setAnswerValue("hi");
        answerOption.setSequence_id(1);

        return List.of(answerOption);
    }




    public QuestionareRequest setQuestionareRequestData() {
        QuestionareRequest questionareRequest = new QuestionareRequest();


        RulesByFlow rulesByFlow = RulesByFlow.builder()
                .flow("flow1.49")
                .ruleId("f146046e-fd6d-481c-bcf6-95aa17ec6d6e")
                .ruleName("rule1")
                .actionId("1928d95f-3eaf-432b-a771-52442f6778e9")
                .condition("requiredQuestionnaireContext==\"condition6\"")
                .lob("lob1")
                .salience(1)
                .isActive(true)
                .isUpdate(false)
                .build();

        Audit audit = Audit.builder()
                .createdTs("2024-12-13 03:52:11.117")
                .createdBy("Default user")
                .modifiedBy("2024-12-13 14:42:33.036")
                .modifiedTs("Default user")
                .build();

        rulesByFlow.setAudit(audit);

        questionareRequest.setRulesByFlow(rulesByFlow);

        Actions actions = Actions.builder()
                .actionId("1928d95f-3eaf-432b-a771-52442f6778e9")
                .actionText("action4")
                .questionIds(Arrays.asList("48a14bb6-6a27-45e2-9428-95d738d56990"))
                .build();

        questionareRequest.setActions(actions);
        List<AnswerOptions> answerOptions = new ArrayList<>();

        AnswerOptions answerOption = AnswerOptions.builder().
                actionId("1928d95f-3eaf-432b-a771-52442f6778e9")
                .questionId("48a14bb6-6a27-45e2-9428-95d738d56990")
                .answerOptionId("107309cd-f218-4a9e-a097-dbbea6eba959")
                .text("I am feeling down or have little energy")
                .value("I am feeling down or have little energy")
                .sequenceId(1)
                .build();

        answerOptions.add(answerOption);
        answerOption = AnswerOptions.builder().
                actionId("1928d95f-3eaf-432b-a771-52442f6778e9")
                .questionId("48a14bb6-6a27-45e2-9428-95d738d56990")
                .answerOptionId("dbb0d95e-2d24-4715-ad19-7e420cef918e")
                .text("I am feeling anxious or panicky")
                .value("I am feeling anxious or panicky")
                .sequenceId(2)
                .build();
        answerOptions.add(answerOption);

        List<Questions> questions = new ArrayList<>();
        Questions question = Questions.builder()
                .actionId("1928d95f-3eaf-432b-a771-52442f6778e9")
                .questionId("48a14bb6-6a27-45e2-9428-95d738d56990")
                .text("What best describes the reason(s) for your visit?")
                .errorMessage("Select a reason for your visit")
                .answerType("checkbox")
                .answerOptionIds(Arrays.asList("107309cd-f218-4a9e-a097-dbbea6eba959",
                        "dbb0d95e-2d24-4715-ad19-7e420cef918e"))
                .answerOptions(answerOptions)
                .build();

        questions.add(question);
        questionareRequest.setQuestions(questions);

        return questionareRequest;
    }

    @Test
    public void testProcessAndStoreInputDataNested() {
        QuestionareRequest questionareRequest = new QuestionareRequest();
        questionareRequest.setRulesByFlow(RulesByFlow.builder()
                .flow("flow1.5")
                .ruleName("rule1")
                .condition("condition5")
                .lob("lob1")
                .salience(1)
                .build());
        questionareRequest.setActions(Actions.builder()
                .actionText("action2")
                .build());
        List<Questions> questions = new ArrayList<>();

// 1. Are you experiencing symptoms?
        questions.add(Questions.builder()
                .questionId("RX#533")
                .text("Are you experiencing symptoms?")
                .answerType("radio")
                .required(true)
                .stacked(true)
                .errorMessage("Select whether you are experiencing symptoms")
                .helpText("What are severe symptoms?")
                .answerOptions(Arrays.asList(
                        AnswerOptions.builder().text("Yes, mild to moderate").value("1").build(),
                        AnswerOptions.builder().text("Yes, severe").value("2").build(),
                        AnswerOptions.builder().text("No").value("3").build()
                ))
                .build()
        );

// 2. Recent COVID-19 exposure
        questions.add(Questions.builder()
                .questionId("CVS#760")
                .text("Do you have any recent exposure to someone with confirmed COVID-19?")
                .answerType("radio")
                .required(true)
                .stacked(false)
                .errorMessage("Select whether you've had recent exposure")
                .answerOptions(Arrays.asList(
                        AnswerOptions.builder().text("Yes").value("1").build(),
                        AnswerOptions.builder().text("No").value("0").build()
                ))
                .build()
        );

// 3. Health insurance with related question
        questions.add(Questions.builder()
                .questionId("TTP#1")
                .text("What type of health insurance do you have?")
                .answerType("radio")
                .required(true)
                .stacked(true)
                .errorMessage("Select what type of health insurance you have")
                .answerOptions(Arrays.asList(
                        AnswerOptions.builder()
                                .text("I have private insurance").value("1")
                                .relatedQuestions(List.of(
                                        Questions.builder()
                                                .questionId("TTP#3")
                                                .text("Do you have Aetna insurance?")
                                                .answerType("radio")
                                                .required(true)
                                                .stacked(false)
                                                .errorMessage("Select whether you have Aetna insurance")
                                                .answerOptions(Arrays.asList(
                                                        AnswerOptions.builder().text("Yes").value("1").build(),
                                                        AnswerOptions.builder().text("No").value("0").build()
                                                ))
                                                .build()
                                ))
                                .build(),
                        AnswerOptions.builder().text("I have Medicare or Tricare").value("2").build(),
                        AnswerOptions.builder().text("I have Medicaid or other government-funded insurance").value("3").build(),
                        AnswerOptions.builder().text("I'm uninsured").value("4").build()
                ))
                .build()
        );

// 4. U.S. veteran
        questions.add(Questions.builder()
                .questionId("CVS#2485")
                .text("Are you a U.S veteran?")
                .answerType("radio")
                .required(true)
                .stacked(true)
                .errorMessage("Select whether you are a U.S veteran")
                .answerOptions(Arrays.asList(
                        AnswerOptions.builder().text("I am a U.S. veteran").value("1").build(),
                        AnswerOptions.builder().text("I am not a U.S. veteran").value("2").build()
                ))
                .build()
        );

// 5. Sex assigned at birth with related questions
        questions.add(Questions.builder()
                .questionId("TTP#2")
                .text("What's your sex assigned at birth?")
                .answerType("radio")
                .required(true)
                .stacked(true)
                .errorMessage("Select your sex assigned at birth")
                .helpText("Why do we ask for sex assigned at birth?")
                .characterLimit(0)
                .answerOptions(Arrays.asList(
                        AnswerOptions.builder()
                                .text("Female").value("1")
                                .relatedQuestions(Arrays.asList(
                                        Questions.builder()
                                                .questionId("RX#540")
                                                .text("Are you pregnant?")
                                                .answerType("radio")
                                                .required(true)
                                                .stacked(false)
                                                .errorMessage("Select whether you are pregnant")
                                                .answerOptions(Arrays.asList(
                                                        AnswerOptions.builder().text("Yes").value("1").build(),
                                                        AnswerOptions.builder().text("No").value("0").build()
                                                ))
                                                .build(),
                                        Questions.builder()
                                                .questionId("RX#541")
                                                .text("Have you given birth in the past 2 weeks?")
                                                .answerType("radio")
                                                .required(true)
                                                .stacked(false)
                                                .errorMessage("Select whether you have given birth in the past 2 weeks")
                                                .answerOptions(Arrays.asList(
                                                        AnswerOptions.builder().text("Yes").value("1").build(),
                                                        AnswerOptions.builder().text("No").value("0").build()
                                                ))
                                                .build(),
                                        Questions.builder()
                                                .questionId("RX#542")
                                                .text("Are you breastfeeding?")
                                                .answerType("radio")
                                                .required(true)
                                                .stacked(false)
                                                .errorMessage("Select whether you are breastfeeding")
                                                .answerOptions(Arrays.asList(
                                                        AnswerOptions.builder().text("Yes").value("1").build(),
                                                        AnswerOptions.builder().text("No").value("0").build()
                                                ))
                                                .build()
                                ))
                                .build(),
                        AnswerOptions.builder().text("Male").value("2").build()
                ))
                .build()
        );

// Set questions to your request
        questionareRequest.setQuestions(questions);

        Map<String, String> reqHdrMap = new HashMap<>();
        reqHdrMap.put("header1", "value1");

        IQEResponse iqeResponse = new IQEResponse();

        Map<String, Object> eventMap = new HashMap<>();

        RulesServiceRepoOrchestrator rulesServiceRepo = mock(RulesServiceRepoOrchestrator.class);

        when(rulesServiceRepo.validateRequest(questionareRequest, iqeResponse)).thenReturn(Mono.just(questionareRequest));

        IQERepoOrchestrator helperClass = mock(IQERepoOrchestrator.class);
        QuestionsRepository questionsRepository = mock(QuestionsRepository.class);
        ActionsRepository actionsRepository = mock(ActionsRepository.class);
        AnswerOptionsRepository answerOptionsRepository = mock(AnswerOptionsRepository.class);

        when(helperClass.assignSequenceIds(questionareRequest)).thenReturn(Mono.just(questionareRequest));
        when(helperClass.processInputData(questionareRequest, reqHdrMap, iqeResponse, eventMap)).thenReturn(Mono.just(questionareRequest));
        when(helperClass.insertQuestionsIntoDB(questionareRequest, eventMap, iqeResponse)).thenAnswer(invocation -> {
            iqeResponse.setStatusCode("0000");
            iqeResponse.setStatusDescription("Data inserted successfully");
            return Mono.just(iqeResponse);
        });

        IQEService iqeServiceClass = new IQEService( questionnaireRulesRepository, loggingUtils, helperClass,
                rulesServiceRepo, actionsRepository, questionsRepository, rulesByFlowRepo, answerOptionsRepository,redisCacheService,questionnaireDetailsRepo);

        Mono<IQEResponse> result = iqeServiceClass.processQuestionnaire(questionareRequest,iqeResponse, reqHdrMap , eventMap);

        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    return response.getStatusCode().equals("0000")
                            && response.getStatusDescription().equals("Data inserted successfully");
                })
                .verifyComplete();
    }

    @Test
    public void testGetQuestionareByActionIdNested() {
        String actionId = "131c1c73-3f2a-4f7d-9252-c90467f9e525";
        QuestionareRequest questionareRequest = new QuestionareRequest();

        RulesByFlowEntity rulesByFlow = setupRulesByFlow();
        ActionsEntity actions = setupActions();
        List<QuestionsEntity> questions = setupQuestionsNested();
        List<AnswerOptionsEntity> answerOptions = setupAnswerOptionsNested();

        RulesByFlowRepository rulesByFlowRepository = mock(RulesByFlowRepository.class);
        QuestionsRepository questionsRepository = mock(QuestionsRepository.class);
        ActionsRepository actionsRepository = mock(ActionsRepository.class);
        AnswerOptionsRepository answerOptionsRepository = mock(AnswerOptionsRepository.class);
        RedisCacheService redisCachingService=mock(RedisCacheService.class);

        when(rulesByFlowRepository.findByActionId(actionId)).thenReturn(Flux.just(rulesByFlow));
        when(actionsRepository.findByActionId(actionId)).thenReturn(Flux.just(actions));
        when(questionsRepository.findByActionId(actionId)).thenReturn(Flux.fromIterable(questions));
        when(answerOptionsRepository.findByActionId(actionId)).thenReturn(Flux.fromIterable(answerOptions));
        when(redisCachingService.getDataFromRedis(Mockito.anyString(),Mockito.anyString(),Mockito.anyMap())).thenReturn(Mono.empty());



        IQEService iqeServiceClass = new IQEService( questionnaireRulesRepository, loggingUtils, helper,
                rulesServiceRepoOrchestrator, actionsRepository, questionsRepository, rulesByFlowRepository, answerOptionsRepository,redisCachingService,questionnaireDetailsRepo);

        Mono<QuestionareRequest> result = iqeServiceClass.questionnaireByActionId(actionId, questionareRequest);

        StepVerifier.create(result)
                .expectNextMatches(request -> {
                    return request.getRulesByFlow().getFlow().equals("flow1.5");
                })
                .verifyComplete();
    }
    @Test
    public void processRelatedQuestion_mapsEntityToDTO() {
        // Arrange
        QuestionsEntity relatedQuestion = QuestionsEntity.builder()
                .questionId("RX#540")
                .questionText("Are you pregnant?")
                .answerType("radio")
                .required(true)
                .stacked(false)
                .errorMessage("Select whether you are pregnant")
                .answerOptionId(Arrays.asList("RX#540_1", "RX#540_0"))
                .sequence_id(0)
                .build();

        List<AnswerOptionsEntity> answerOptionsList = Arrays.asList(
                AnswerOptionsEntity.builder()
                        .questionId("RX#540")
                        .answerOptionId("RX#540_1")
                        .answerText("Yes")
                        .answerValue("1")
                        .sequence_id(0)
                        .build(),
                AnswerOptionsEntity.builder()
                        .questionId("RX#540")
                        .answerOptionId("RX#540_0")
                        .answerText("No")
                        .answerValue("0")
                        .sequence_id(1)
                        .build()
        );

        List<QuestionsEntity> questions = List.of(relatedQuestion);

        IQERepoOrchestrator orchestrator = new IQERepoOrchestrator(
                actionsRepo, questionsRepo, rulesByFlowRepo, questionnaireDetailsRepo, answerOptionsRepo, redisCacheService
        );

        // Act & Assert
        StepVerifier.create(orchestrator.processRelatedQuestion(relatedQuestion, answerOptionsList, questions))
                .assertNext(q -> {
                    assertEquals("RX#540", q.getQuestionId());
                    assertEquals("Are you pregnant?", q.getText());
                    assertEquals(2, q.getAnswerOptions().size());
                    assertTrue(q.getAnswerOptions().stream().anyMatch(a -> a.getText().equals("Yes")));
                    assertTrue(q.getAnswerOptions().stream().anyMatch(a -> a.getText().equals("No")));
                })
                .verifyComplete();
    }
    @Test
    public void processQuestionnaire_mapsQuestionAndAnswerOptions() {
        QuestionsEntity questionEntity = QuestionsEntity.builder()
                .questionId("RX#533")
                .questionText("Are you experiencing symptoms?")
                .answerType("radio")
                .required(true)
                .stacked(true)
                .errorMessage("Select whether you are experiencing symptoms")
                .helpText("What are severe symptoms?")
                .answerOptionId(Arrays.asList("RX#533_1", "RX#533_2", "RX#533_3"))
                .sequence_id(1)
                .build();

        List<AnswerOptionsEntity> answerOptionsList = Arrays.asList(
                AnswerOptionsEntity.builder().questionId("RX#533").answerOptionId("RX#533_1").answerText("Yes, mild to moderate").answerValue("1").sequence_id(1).build(),
                AnswerOptionsEntity.builder().questionId("RX#533").answerOptionId("RX#533_2").answerText("Yes, severe").answerValue("2").sequence_id(2).build(),
                AnswerOptionsEntity.builder().questionId("RX#533").answerOptionId("RX#533_3").answerText("No").answerValue("3").sequence_id(3).build()
        );

        List<QuestionsEntity> questions = List.of(questionEntity);

        IQERepoOrchestrator orchestrator = new IQERepoOrchestrator(
                actionsRepo, questionsRepo, rulesByFlowRepo, questionnaireDetailsRepo, answerOptionsRepo, redisCacheService
        );

        StepVerifier.create(orchestrator.processQuestionnaire(questionEntity, answerOptionsList, questions))
                .assertNext(q -> {
                    assertEquals("RX#533", q.getQuestionId());
                    assertEquals("Are you experiencing symptoms?", q.getText());
                    assertEquals(3, q.getAnswerOptions().size());
                    assertTrue(q.getAnswerOptions().stream().anyMatch(a -> a.getText().equals("Yes, mild to moderate")));
                    assertTrue(q.getAnswerOptions().stream().anyMatch(a -> a.getText().equals("No")));
                })
                .verifyComplete();
    }

    @Test
    public void processQuestionnaire_mapsNestedRelatedQuestions() {
        QuestionsEntity relatedQuestion = QuestionsEntity.builder()
                .questionId("RX#540")
                .questionText("Are you pregnant?")
                .answerType("radio")
                .required(true)
                .stacked(false)
                .errorMessage("Select whether you are pregnant")
                .answerOptionId(Arrays.asList("RX#540_1", "RX#540_0"))
                .sequence_id(1)
                .build();

        QuestionsEntity mainQuestion = QuestionsEntity.builder()
                .questionId("TTP#2")
                .questionText("What's your sex assigned at birth?")
                .answerType("radio")
                .required(true)
                .stacked(true)
                .errorMessage("Select your sex assigned at birth")
                .answerOptionId(Arrays.asList("TTP#2_1", "TTP#2_2"))
                .sequence_id(1)
                .build();

        AnswerOptionsEntity femaleOption = AnswerOptionsEntity.builder()
                .questionId("TTP#2")
                .answerOptionId("TTP#2_1")
                .answerText("Female")
                .answerValue("1")
                .relatedQuestions(List.of("RX#540"))
                .sequence_id(1)
                .build();

        AnswerOptionsEntity maleOption = AnswerOptionsEntity.builder()
                .questionId("TTP#2")
                .answerOptionId("TTP#2_2")
                .answerText("Male")
                .answerValue("2")
                .sequence_id(2)
                .build();

        AnswerOptionsEntity yesPregnant = AnswerOptionsEntity.builder()
                .questionId("RX#540")
                .answerOptionId("RX#540_1")
                .answerText("Yes")
                .answerValue("1")
                .sequence_id(1)
                .build();

        AnswerOptionsEntity noPregnant = AnswerOptionsEntity.builder()
                .questionId("RX#540")
                .answerOptionId("RX#540_0")
                .answerText("No")
                .answerValue("0")
                .sequence_id(2)
                .build();

        List<QuestionsEntity> questions = Arrays.asList(mainQuestion, relatedQuestion);
        List<AnswerOptionsEntity> answerOptionsList = Arrays.asList(femaleOption, maleOption, yesPregnant, noPregnant);

        IQERepoOrchestrator orchestrator = new IQERepoOrchestrator(
                actionsRepo, questionsRepo, rulesByFlowRepo, questionnaireDetailsRepo, answerOptionsRepo, redisCacheService
        );

        StepVerifier.create(orchestrator.processQuestionnaire(mainQuestion, answerOptionsList, questions))
                .assertNext(q -> {
                    assertEquals("TTP#2", q.getQuestionId());
                    assertEquals(2, q.getAnswerOptions().size());
                    AnswerOptions female = q.getAnswerOptions().stream().filter(a -> a.getText().equals("Female")).findFirst().orElse(null);
                    assertNotNull(female);
                    assertNotNull(female.getRelatedQuestions());
                    assertEquals(1, female.getRelatedQuestions().size());
                    assertEquals("RX#540", female.getRelatedQuestions().get(0).getQuestionId());
                })
                .verifyComplete();
    }


    // Test for extractAnswerOptionsRecursive
    @Test
    public void extractAnswerOptionsRecursive_flattensAllOptionsIncludingNested() {
        Questions nested = Questions.builder()
                .questionId("Q2")
                .answerOptions(List.of(
                        AnswerOptions.builder().answerOptionId("Q2_A1").text("Nested Option").build()
                ))
                .build();
        Questions main = Questions.builder()
                .questionId("Q1")
                .answerOptions(List.of(
                        AnswerOptions.builder()
                                .answerOptionId("Q1_A1")
                                .text("Main Option")
                                .relatedQuestions(List.of(nested))
                                .build()
                ))
                .build();
        IQERepoOrchestrator orchestrator = new IQERepoOrchestrator(
                actionsRepo, questionsRepo, rulesByFlowRepo, questionnaireDetailsRepo, answerOptionsRepo, redisCacheService
        );


        List<AnswerOptionsEntity> result = orchestrator.extractAnswerOptionsRecursive(main).collectList().block();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(a -> "Q1_A1".equals(a.getAnswerOptionId())));
        assertTrue(result.stream().anyMatch(a -> "Q2_A1".equals(a.getAnswerOptionId())));
    }


    // Test for extractQuestionsRecursive
    @Test
    public void extractQuestionsRecursive_flattensAllQuestionsIncludingNested() {
        Questions nested = Questions.builder()
                .questionId("Q2")
                .text("Nested Question")
                .answerOptions(new ArrayList<>())
                .build();
        Questions main = Questions.builder()
                .questionId("Q1")
                .text("Main Question")
                .answerOptions(List.of(
                        AnswerOptions.builder().relatedQuestions(List.of(nested)).build()
                ))
                .build();
        IQERepoOrchestrator orchestrator = new IQERepoOrchestrator(
                actionsRepo, questionsRepo, rulesByFlowRepo, questionnaireDetailsRepo, answerOptionsRepo, redisCacheService
        );


        List<QuestionsEntity> result = orchestrator.extractQuestionsRecursive(main).collectList().block();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(q -> "Q1".equals(q.getQuestionId())));
        assertTrue(result.stream().anyMatch(q -> "Q2".equals(q.getQuestionId())));
    }



}