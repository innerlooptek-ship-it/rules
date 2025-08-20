package com.cvshealth.digital.microservice.iqe.service;



import com.cvshealth.digital.microservice.iqe.controller.IQEController;
import com.cvshealth.digital.microservice.iqe.dto.*;
import com.cvshealth.digital.microservice.iqe.entity.QuestionnaireRules;
import com.cvshealth.digital.microservice.iqe.entity.RulesByFlowEntity;
import com.cvshealth.digital.microservice.iqe.exception.CvsException;
import com.cvshealth.digital.microservice.iqe.model.IQEResponse;
import com.cvshealth.digital.microservice.iqe.model.Question;
import com.cvshealth.digital.microservice.iqe.model.RulesDetails;
import com.cvshealth.digital.microservice.iqe.utils.LoggingUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.tomcat.util.json.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.*;

import static com.cvshealth.digital.microservice.iqe.constants.SchedulingConstants.ERROR_INTERNAL_SERVER_ERROR;
import static com.cvshealth.digital.microservice.iqe.constants.SchedulingConstants.INTERNAL_SERVER_ERROR_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = IQEController.class)
@ContextConfiguration(classes = {IQEController.class} )
@Import({IQEService.class})
public class IQEControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    IQEController iqeController;

    @Mock
    private IQEService  iqeService;

    @Mock
    LoggingUtils loggingUtils;

    private static final Map<String, String> errorMessages = Map.of(INTERNAL_SERVER_ERROR_MESSAGE, "Internal server error occurred");


    public Map<String, String> mockHttpHeaders()
    {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return headers;
    }

    @BeforeEach
    public void setUp() {
        webTestClient = webTestClient.mutate()
                .responseTimeout(Duration.ofMillis(30000))
                .build();



        MockitoAnnotations.initMocks(this);
    }


    @Test()
   public void getQuestionaireTestRulesBadRequest() throws ParseException, JsonProcessingException {
        RulesDetails rulesDetails = new RulesDetails();
        List<QuestionnaireRules> questionnaireRulesList = new ArrayList<>();

        Mono<com.cvshealth.digital.microservice.iqe.model.Questions> emptyQuestionsMono = Mono.empty();


        createQuestionaireRulesList(questionnaireRulesList);

        Mockito.when(iqeService.getRuleDetails(ArgumentMatchers.any(), ArgumentMatchers.anyMap(),ArgumentMatchers.anyMap())).thenReturn(emptyQuestionsMono);


        webTestClient.post().uri("/schedule/iqe/v1/getquestionnairerules")
                .header("x-grid","123")
                .header("experienceId", "432133")
                .header("cat", "NSG")
                .bodyValue(rulesDetails)
                .exchange()
                .expectStatus()
                .isBadRequest().expectBody(com.cvshealth.digital.microservice.iqe.model.Questions.class);

    }


    @Test()
    public void getQuestionaireNoContent() {

        Mono<com.cvshealth.digital.microservice.iqe.model.Questions> emptyQuestionsMono = Mono.empty();

        RulesDetails rulesDetails = new RulesDetails();
        rulesDetails.setFlow("MC_CORE");
        List<QuestionnaireRules> questionnaireRulesList = new ArrayList<>();

        createQuestionaireRulesList(questionnaireRulesList);
        Mockito.when(iqeService.getRuleDetails(ArgumentMatchers.any(), ArgumentMatchers.anyMap(),ArgumentMatchers.anyMap())).thenReturn(emptyQuestionsMono);

        webTestClient.post().uri("/schedule/iqe/v1/getquestionnairerules")
                .header("x-grid","123")
                .header("experienceId", "432133")
                .header("cat", "NSG")
                .bodyValue(rulesDetails)
                .exchange()
                .expectStatus()
                .isOk().expectBody().isEmpty();
    }


    @Test()
    public void getQuestionaireSuccess() {

        com.cvshealth.digital.microservice.iqe.model.Questions questions = new com.cvshealth.digital.microservice.iqe.model.Questions();
        ArrayList<Question> questionsList = new ArrayList<>();
        Question question = new Question();
        question.setId("10001");
        questionsList.add(question);
        questions.setQuestions(questionsList);
        Mono<com.cvshealth.digital.microservice.iqe.model.Questions> questionsMono = Mono.just(questions);

        RulesDetails rulesDetails = new RulesDetails();
        rulesDetails.setFlow("MC_CORE");
        rulesDetails.setReasonId(89);
        rulesDetails.setRequiredQuestionnaireContext("MC_CORE_ELIGIBILITY_QUESTION");
        List<QuestionnaireRules> questionnaireRulesList = new ArrayList<>();

        createQuestionaireRulesList(questionnaireRulesList);
        Mockito.when(iqeService.getRuleDetails(ArgumentMatchers.any(), ArgumentMatchers.anyMap(),ArgumentMatchers.anyMap())).thenReturn(questionsMono);

        webTestClient.post().uri("/schedule/iqe/v1/getquestionnairerules")
                .header("x-grid","123")
                .header("experienceId", "432133")
                .header("cat", "NSG")
                .bodyValue(rulesDetails)
                .exchange()
                .expectStatus()
                .isOk().expectBody(com.cvshealth.digital.microservice.iqe.model.Questions.class);

    }

    private static void createQuestionaireRulesList(List<QuestionnaireRules> questionnaireRulesList) {
        QuestionnaireRules questionnaireRules = new QuestionnaireRules();
        questionnaireRules.setAction("\"test\"");
        questionnaireRules.setCondition("requiredQuestionnaireContext == \"MC_CORE_ELIGIBILITY_QUESTION\" && reasonId==86");
        questionnaireRules.setFlow("MC_CORE");
        questionnaireRules.setRuleName("Wound_Care_MCCORE");
        questionnaireRules.setId("10009");
        questionnaireRules.setSalience(100);

        questionnaireRulesList.add(questionnaireRules);
    }


    @Test
    public void testCreateQuestionare_PositiveFlow() {

        IQEResponse iqeResponse = new IQEResponse();
        iqeResponse.setStatusCode("0000");
        iqeResponse.setStatusDescription("Data inserted successfully");
        iqeResponse.setActionId("131c1c73-3f2a-4f7d-9252-c90467f9e525");

        Mono<IQEResponse> iqeResponseMono = Mono.just(iqeResponse);

        QuestionareRequest questionareRequest = new QuestionareRequest();

        setQuestionareRequest( questionareRequest);

        Mockito.when(iqeService.processQuestionnaire(ArgumentMatchers.any(),ArgumentMatchers.any(), ArgumentMatchers.anyMap(),  ArgumentMatchers.anyMap())).thenReturn(iqeResponseMono);

        StepVerifier.create(webTestClient.post().uri("/schedule/iqe/v1/questionnaire")
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .bodyValue(questionareRequest)
                        .exchange()
                        .returnResult(IQEResponse.class)
                        .getResponseBody())
                .expectNextMatches(response -> {
                    return response.getStatusCode().equals("0000")
                            && response.getStatusDescription().equals("Data inserted successfully");
                })
                .verifyComplete();
    }

    @Test
    public void testCreateQuestionare_NegativeFlow() {

        IQEResponse iqeResponse = new IQEResponse();
        iqeResponse.setStatusCode("5007");
        iqeResponse.setStatusDescription("Questions already exist for the flow and condition");

        Mono<IQEResponse> iqeResponseMono = Mono.just(iqeResponse);

        QuestionareRequest questionareRequest = new QuestionareRequest();

        setQuestionareRequest( questionareRequest);

        Mockito.when(iqeService.processQuestionnaire(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.anyMap(), ArgumentMatchers.anyMap())).thenReturn(iqeResponseMono);

        StepVerifier.create(webTestClient.post().uri("/schedule/iqe/v1/questionnaire")
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .bodyValue(questionareRequest)
                        .exchange()
                        .returnResult(IQEResponse.class)
                        .getResponseBody())
                .expectNextMatches(response -> {
                    return response.getStatusCode().equals("5007")
                            && response.getStatusDescription().equals("Questions already exist for the flow and condition");
                })
                .verifyComplete();
    }

    @Test
    public void testGetAllRules_PositiveFlow() {

        QuestionareRequest questionareRequest = new QuestionareRequest();

        questionareRequest.setActiveRules(List.of(setRulesByFlow(new RulesByFlowEntity())));

        Mono<QuestionareRequest> questionareRequestMono = Mono.just(questionareRequest);

        Mockito.when(iqeService.rules()).thenReturn(questionareRequestMono);

        StepVerifier.create(webTestClient.get().uri("/schedule/iqe/v1/rules")
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .exchange()
                        .returnResult(QuestionareRequest.class)
                        .getResponseBody())
                .expectNextMatches(response -> {
                    return response.getActiveRules().size() == 1
                            && response.getActiveRules().get(0).getFlow().equals("flow1.4");
                })
                .verifyComplete();
    }

    @Test
    public void testGetQuestionareByActionId_PositiveFlow() {

        String actionId = "131c1c73-3f2a-4f7d-9252-c90467f9e525";

        RulesByFlow rulesByFlow = RulesByFlow.builder()
                .flow("flow1.5")
                .ruleId("748b1a95-87a4-400a-ac5b-97330699d4d2")
                .ruleName("rule1")
                .actionId("131c1c73-3f2a-4f7d-9252-c90467f9e525")
                .condition("condition5")
                .lob("lob1")
                .salience(1)
                .audit(Audit.builder()
                        .createdTs("2024-12-05 14:59:03.817")
                        .createdBy("Default user")
                        .build())
                .isActive(true)
                .isUpdate(false)
                .build();

        Actions actions = Actions.builder()
                .actionId("131c1c73-3f2a-4f7d-9252-c90467f9e525")
                .actionText("action2")
                .questionIds(List.of("763944ca-9d67-4096-a9ca-32bb7ee17490"))
                .build();

        AnswerOptions answerOptions = AnswerOptions.builder()
                .actionId("131c1c73-3f2a-4f7d-9252-c90467f9e525")
                .questionId("763944ca-9d67-4096-a9ca-32bb7ee17490")
                .answerOptionId("339d7fb2-d8fb-4184-a697-19834376e6ea")
                .text("answer1")
                .value("hi")
                .sequenceId(1)
                .build();

        Questions questions = Questions.builder()
                .actionId("131c1c73-3f2a-4f7d-9252-c90467f9e525")
                .questionId("763944ca-9d67-4096-a9ca-32bb7ee17490")
                .text("question1")
                .errorMessage("Please enter valid question")
                .answerType("radio")
                .answerOptionIds(List.of("339d7fb2-d8fb-4184-a697-19834376e6ea"))
                .answerOptions(List.of(answerOptions))
                .helpText("")
                .characterLimit(0)
                .stacked(false)
                .sequenceId(1)
                .build();

        QuestionareRequest request = new QuestionareRequest();
        request.setRulesByFlow(rulesByFlow);
        request.setActions(actions);
        request.setQuestions(List.of(questions));


        Mono<QuestionareRequest> expectedResponseMono = Mono.just(request);

        Mockito.when(iqeService.questionnaireByActionId(Mockito.eq(actionId), Mockito.any())).thenReturn(expectedResponseMono);

        StepVerifier.create(webTestClient.get().uri("/schedule/iqe/v1/questionnaire/" + actionId)
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .exchange()
                        .returnResult(QuestionareRequest.class)
                        .getResponseBody())
                .expectNextMatches(response -> response.getRulesByFlow().getFlow().equals("flow1.5"))
                .verifyComplete();
    }

    @Test
    public void testGetQuestionareByActionId_NegativeFlow() {

        String actionId = "131c1c73-3f2a-4f7d-9252-c90467f9e525";

        QuestionareRequest errorRequest = new QuestionareRequest();
        errorRequest.setErrorDescription("Questionare not found for the given flow and condition");
        errorRequest.setStatusCode("5008");

        Mono<QuestionareRequest> errorMono = Mono.just(errorRequest);

        Mockito.when(iqeService.questionnaireByActionId(Mockito.eq(actionId), Mockito.any())).thenReturn(errorMono);

        StepVerifier.create(webTestClient.get().uri("/schedule/iqe/v1/questionnaire/" + actionId)
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .exchange()
                        .returnResult(QuestionareRequest.class)
                        .getResponseBody())
                .expectNextMatches(response -> response.getErrorDescription().equals("Questionare not found for the given flow and condition")
                        && response.getStatusCode().equals("5008"))
                .verifyComplete();
    }

    public void setQuestionareRequest(QuestionareRequest questionareRequest) {
        questionareRequest.setRulesByFlow(RulesByFlow.builder()
                .flow("flow1.51")
                .ruleName("rule1")
                .condition("condition5")
                .lob("lob1")
                .build());
        questionareRequest.setActions(Actions.builder()
                .actionText("action2")
                .build());
        questionareRequest.setQuestions(Collections.singletonList(Questions.builder()
                .text("question1")
                .errorMessage("Please enter valid question")
                .answerType("radio")
                .answerOptions(Collections.singletonList(AnswerOptions.builder()
                        .text("answer1")
                        .value("hi")
                        .build()))
                .build()));

    }

    public RulesByFlowEntity setRulesByFlow(RulesByFlowEntity rulesByFlow) {

        rulesByFlow.setFlow("flow1.4");
        rulesByFlow.setRuleId( "97e60d8c-656a-40c5-afd9-04a117a40ff5");
        rulesByFlow.setRuleName( "rule1");
        rulesByFlow.setActionId("50eefa18-cd7c-47dd-9de4-d3374b3f5d6b");
        rulesByFlow.setCondition("condition5");
        rulesByFlow.setLob( "lob1");
        rulesByFlow.setSalience(1);
        rulesByFlow.setActive(true);

        return rulesByFlow;
    }


    @Test
    public void testDeleteQuestionareByActionId_PositiveFlow() {

        String actionId = "131c1c73-3f2a-4f7d-9252-c90467f9e525";

        IQEResponse expectedResponse = new IQEResponse();
        expectedResponse.setStatusCode("0000");
        expectedResponse.setStatusDescription("ActionId deleted successfully");

        Mono<IQEResponse> expectedResponseMono = Mono.just(expectedResponse);

        Mockito.when(iqeService.deleteQuestionnaireByActionId(Mockito.eq(actionId))).thenReturn(expectedResponseMono);

        StepVerifier.create(webTestClient.delete().uri("/schedule/iqe/v1/questionnaire/" + actionId)
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .exchange()
                        .returnResult(IQEResponse.class)
                        .getResponseBody())
                .expectNextMatches(response -> response.getStatusCode().equals("0000")
                        && response.getStatusDescription().equals("ActionId deleted successfully"))
                .verifyComplete();
    }


    @Test
    public void testDeleteQuestionareByActionId_NegativeFlow() {

        String actionId = "131c1c73-3f2a-4f7d-9252-c90467f9e525";

        IQEResponse expectedResponse = new IQEResponse();
        expectedResponse.setStatusCode("5009");
        expectedResponse.setActionId("ActionId not found");

        Mono<IQEResponse> expectedResponseMono = Mono.just(expectedResponse);

        Mockito.when(iqeService.deleteQuestionnaireByActionId(Mockito.eq(actionId))).thenReturn(expectedResponseMono);

        StepVerifier.create(webTestClient.delete().uri("/schedule/iqe/v1/questionnaire/" + actionId)
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .exchange()
                        .returnResult(IQEResponse.class)
                        .getResponseBody())
                .expectNextMatches(response -> response != null
                        && response.getStatusCode().equals("5009")
                        && response.getActionId() .equals("ActionId not found") )
                .verifyComplete();

    }


    @Test
    public void questionnaireByActionAndQuestionId_validData()
    {
        String actionId = "1928d95f-3eaf-432b-a771-52442f6778e9";
        String questionId = "48a14bb6-6a27-45e2-9428-95d738d56990";

        QuestionareRequest iqeOutPut =setQuestionareRequestData();


        iqeOutPut.setQuestion(iqeOutPut.getQuestions().get(0));

        Mono<QuestionareRequest> expectedResponseMono = Mono.just(iqeOutPut);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        Mockito.when(iqeService.questionnaireByActionAndQuestionId(Mockito.eq(actionId), Mockito.eq(questionId),
                Mockito.any())).thenReturn(expectedResponseMono);

        StepVerifier.create(webTestClient.get().uri("/schedule/iqe/v1/questionnaire/" + actionId + "/" + questionId)
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .exchange()
                        .returnResult(QuestionareRequest.class)
                        .getResponseBody())
                .expectNextMatches(response -> response.getQuestion()!=null && response.getQuestion().getQuestionId()
                        .equals("48a14bb6-6a27-45e2-9428-95d738d56990"))
                .verifyComplete();

    }



    @Test
    public void questionnaireByActionAndQuestionId_inValidData()
    {
        String actionId = "1928d95f-3eaf-432b-a771-52442f6778e9";
        String questionId = "48a14bb6-6a27-45e2-9428-95d738d56990";

        Mono<QuestionareRequest> expectedResponseMono = Mono.empty();

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        Mockito.when(iqeService.questionnaireByActionAndQuestionId(Mockito.eq(actionId), Mockito.eq(questionId),
                Mockito.any())).thenReturn(expectedResponseMono);

        StepVerifier.create(webTestClient.get().uri("/schedule/iqe/v1/questionnaire/" + actionId + "/48a14bb6-6a27-45e2-9428-95d738d56990")
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .exchange()
                        .returnResult(QuestionareRequest.class)
                        .getResponseBody())
                .expectNextCount(0)
                .verifyComplete();
    }


    @Test
    public void questionnaireByFlow_validData()
    {

        RulesDetails rulesDetails = new RulesDetails();
        rulesDetails.setFlow("MC_CORE");
        rulesDetails.setRequiredQuestionnaireContext("condition6");

        QuestionareRequest iqeOutPut =setQuestionareRequestData();
        Mono<QuestionareRequest> expectedResponseMono = Mono.just(iqeOutPut);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        Mockito.when(iqeService.questionnaireByFlow(Mockito.any(),Mockito.any(), Mockito.anyMap())).thenReturn(expectedResponseMono);

        StepVerifier.create(webTestClient.post().uri("/schedule/iqe/v1/questionnaire/evaluation-by-flow")
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .bodyValue(rulesDetails)
                        .exchange()
                        .returnResult(QuestionareRequest.class)
                        .getResponseBody())
                    .expectNextMatches(response -> response.getActions()!=null && response.getActions().getQuestionIds().get(0)
                        .equals("48a14bb6-6a27-45e2-9428-95d738d56990"))
                          .verifyComplete();
    }


    @Test
    public void questionnaireByFlow_inValidData()
    {

        RulesDetails rulesDetails = new RulesDetails();
        rulesDetails.setFlow("MC_CORE");
        rulesDetails.setRequiredQuestionnaireContext("condition6");

        Mono<QuestionareRequest> expectedResponseMono = Mono.empty();

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        Mockito.when(iqeService.questionnaireByFlow(Mockito.any(),Mockito.any(), Mockito.anyMap())).thenReturn(expectedResponseMono);

        StepVerifier.create(webTestClient.post().uri("/schedule/iqe/v1/questionnaire/evaluation-by-flow")
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .bodyValue(rulesDetails)
                        .exchange()
                        .returnResult(QuestionareRequest.class)
                        .getResponseBody())
                .expectNextCount(0)
                .verifyComplete();
    }


    @Test
    public void questionnaireByFlowAndCondition_validData()
    {

        RulesDetails rulesDetails = new RulesDetails();
        rulesDetails.setFlow("MC_CORE");
        rulesDetails.setRequiredQuestionnaireContext("condition6");

        QuestionareRequest iqeOutPut =setQuestionareRequestData();
        Mono<QuestionareRequest> expectedResponseMono = Mono.just(iqeOutPut);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        Mockito.when(iqeService.questionnaireByFlowAndCondition(Mockito.any(),Mockito.any(), Mockito.anyMap())).thenReturn(expectedResponseMono);

        StepVerifier.create(webTestClient.post().uri("/schedule/iqe/v1/questionnaires/dynamic-flow-condition-evaluation")
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .bodyValue(rulesDetails)
                        .exchange()
                        .returnResult(QuestionareRequest.class)
                        .getResponseBody())
                .expectNextMatches(response -> response.getActions()!=null && response.getActions().getQuestionIds().get(0)
                        .equals("48a14bb6-6a27-45e2-9428-95d738d56990"))
                .verifyComplete();
    }
    @Test
    public void questionnaireByFlowAndCondition_validData_VM() {
        RulesDetails rulesDetails = new RulesDetails();
        rulesDetails.setFlow("VM");
        rulesDetails.setRequiredQuestionnaireContext("HPI");

        QuestionareRequest iqeOutPut = setQuestionareRequestData_VM();
        Mono<QuestionareRequest> expectedResponseMono = Mono.just(iqeOutPut);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        Mockito.when(iqeService.questionnaireByFlowAndCondition(Mockito.any(), Mockito.any(), Mockito.anyMap())).thenReturn(expectedResponseMono);

        StepVerifier.create(webTestClient.post().uri("/schedule/iqe/v1/questionnaires/dynamic-flow-condition-evaluation")
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .bodyValue(rulesDetails)
                        .exchange()
                        .returnResult(QuestionareRequest.class)
                        .getResponseBody())
                .expectNextMatches(response -> {
                    // Adjust the predicate to match the expected values
                    return response.getActions() != null &&
                            response.getActions().getQuestionIds().contains("HPI_symptoms") &&
                            response.getActions().getQuestionIds().contains("HPI_symptoms_timeline_unit") &&
                            response.getActions().getQuestionIds().contains("HPI_symptoms_check") &&
                            response.getActions().getQuestionIds().contains("HPI_risk_factors") &&
                    response.getActions().getDetailIds().contains("detail1") &&
                            response.getActions().getDetailIds().contains("detail2");
                })
                .verifyComplete();
    }


    @Test
    public void questionnaireByFlowAndCondition_inValidData()
    {

        RulesDetails rulesDetails = new RulesDetails();
        rulesDetails.setFlow("MC_CORE");
        rulesDetails.setRequiredQuestionnaireContext("condition6");

        Mono<QuestionareRequest> expectedResponseMono = Mono.empty();

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        Mockito.when(iqeService.questionnaireByFlowAndCondition(Mockito.any(),Mockito.any(), Mockito.anyMap())).thenReturn(expectedResponseMono);

        StepVerifier.create(webTestClient.post().uri("/schedule/iqe/v1/questionnaires/dynamic-flow-condition-evaluation")
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .bodyValue(rulesDetails)
                        .exchange()
                        .returnResult(QuestionareRequest.class)
                        .getResponseBody())
                .expectNextCount(0)
                .verifyComplete();
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
    public QuestionareRequest setQuestionareRequestData_VM() {
        RulesByFlow rulesByFlow = RulesByFlow.builder()
                .flow("VM")
                .ruleId("081fbade-89ea-4c30-a5c4-218e9d753017")
                .ruleName("rule1")
                .actionId("5d57aa5b-9045-42c2-992f-ed5342083616")
                .condition("requiredQuestionnaireContext==\"HPI\"")
                .lob("VM")
                .salience(1)
                .isActive(true)
                .audit(Audit.builder()
                        .createdTs("2024-12-13 03:52:11.117")
                        .createdBy("Default user")
                        .modifiedTs("2024-12-13 14:42:33.036")
                        .modifiedBy("Default user")
                        .build())
                .isUpdate(false)
                .build();

        Actions actions = Actions.builder()
                .actionId("5d57aa5b-9045-42c2-992f-ed5342083616")
                .actionText("Returns the Questionnaire for VM HPI CONTEXT")
                .questionIds(Arrays.asList("HPI_symptoms", "HPI_symptoms_timeline_unit", "HPI_symptoms_check", "HPI_risk_factors"))
                .detailIds(Arrays.asList("detail1", "detail2")) // Set detail IDs here
                .build();

        List<Questions> questions = Arrays.asList(
                Questions.builder()
                        .actionId("1928d95f-3eaf-432b-a771-52442f6778e9")
                        .questionId("48a14bb6-6a27-45e2-9428-95d738d56990")
                        .text("What best describes the reason(s) for your visit?")
                        .errorMessage("Select a reason for your visit")
                        .answerType("checkbox")
                        .answerOptionIds(Arrays.asList("107309cd-f218-4a9e-a097-dbbea6eba959", "dbb0d95e-2d24-4715-ad19-7e420cef918e"))
                        .answerOptions(Arrays.asList(
                                AnswerOptions.builder()
                                        .actionId("5d57aa5b-9045-42c2-992f-ed5342083616")
                                        .questionId("HPI_symptoms")
                                        .answerOptionId("00e14768-0d84-4aff-ba5d-62b7e6af1cc8")
                                        .text("Decreased appetite")
                                        .value("Decreased appetite")
                                        .sequenceId(1)
                                        .build(),
                                AnswerOptions.builder()
                                        .actionId("1928d95f-3eaf-432b-a771-52442f6778e9")
                                        .questionId("48a14bb6-6a27-45e2-9428-95d738d56990")
                                        .answerOptionId("dbb0d95e-2d24-4715-ad19-7e420cef918e")
                                        .text("I am feeling anxious or panicky")
                                        .value("I am feeling anxious or panicky")
                                        .sequenceId(2)
                                        .build()))
                        .build());

        List<Details> details = Arrays.asList(
                Details.builder()
                        .title("Detail Title 1")
                        .instructions("Detail Instructions 1")
                        .helper("Detail Helper 1")
                        .subContext("Detail SubContext 1")
                        .pageNumber(1)
                        .footer("Detail Footer 1")
                        .build(),
                Details.builder()
                        .title("Detail Title 2")
                        .instructions("Detail Instructions 2")
                        .helper("Detail Helper 2")
                        .subContext("Detail SubContext 2")
                        .pageNumber(2)
                        .footer("Detail Footer 2")
                        .build());

        QuestionareRequest request = new QuestionareRequest();
        request.setRulesByFlow(rulesByFlow);
        request.setActions(actions);
        request.setQuestions(questions);
        request.setDetails(details);
        return request;
    }

    @Test
    public void questionsByActionAndQuestionId_validData() {
        RelatedQuestionsRequest relatedQuestions = new RelatedQuestionsRequest();
        // Set up relatedQuestions as needed for your test

        QuestionareRequest expectedResponse = new QuestionareRequest();
        // Set up expectedResponse as needed

        Mockito.when(iqeService.getQuestionsByRelatedQuestionsList(
                        Mockito.eq(relatedQuestions), Mockito.any(), Mockito.anyMap()))
                .thenReturn(Mono.just(expectedResponse));

        StepVerifier.create(webTestClient.post().uri("/schedule/iqe/v1/questionnaire/related-questions")
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .bodyValue(relatedQuestions)
                        .exchange()
                        .returnResult(QuestionareRequest.class)
                        .getResponseBody())
                .expectNextMatches(response -> response != null)
                .verifyComplete();
    }

    @Test
    public void questionsByActionAndQuestionId_noContent() {
        RelatedQuestionsRequest relatedQuestions = new RelatedQuestionsRequest();
        // Set up relatedQuestions as needed

        Mockito.when(iqeService.getQuestionsByRelatedQuestionsList(
                        Mockito.eq(relatedQuestions), Mockito.any(), Mockito.anyMap()))
                .thenReturn(Mono.empty());

        StepVerifier.create(webTestClient.post().uri("/schedule/iqe/v1/questionnaire/related-questions")
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .bodyValue(relatedQuestions)
                        .exchange()
                        .returnResult(QuestionareRequest.class)
                        .getResponseBody())
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    public void questionsByActionAndQuestionId_error() {
        RelatedQuestionsRequest relatedQuestions = new RelatedQuestionsRequest();
        // Set up relatedQuestions as needed

        Mockito.when(iqeService.getQuestionsByRelatedQuestionsList(
                        Mockito.eq(relatedQuestions), Mockito.any(), Mockito.anyMap()))
                .thenReturn(Mono.error(new RuntimeException("Test error")));

        StepVerifier.create(webTestClient.post().uri("/schedule/iqe/v1/questionnaire/related-questions")
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .bodyValue(relatedQuestions)
                        .exchange()
                        .returnResult(QuestionareRequest.class)
                        .getResponseBody())
                .expectNextMatches(response -> response != null && response.getErrorDescription() == null)
                .verifyComplete();
    }
    Mono<String> errorHandlingMono(Throwable error) {
        return Mono.<String>error(error)
                .onErrorResume(e -> {
                    if (e instanceof CvsException) {
                        return Mono.error(e);
                    }
                    return Mono.error(
                            new CvsException(
                                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                    ERROR_INTERNAL_SERVER_ERROR,
                                    errorMessages.get(INTERNAL_SERVER_ERROR_MESSAGE),
                                    errorMessages.get(INTERNAL_SERVER_ERROR_MESSAGE),
                                    e.getMessage()
                            )
                    );
                });
    }

    @Test
    void testOnErrorResume_withCvsException() {
        CvsException cvsException = new CvsException(400, "CODE", "msg", "desc", "details");
        StepVerifier.create(errorHandlingMono(cvsException))
                .expectErrorMatches(throwable -> throwable instanceof CvsException && throwable == cvsException)
                .verify();
    }

}