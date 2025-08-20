package com.cvshealth.digital.microservice.iqe.controller;

import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
import com.cvshealth.digital.microservice.iqe.dto.RelatedQuestionsRequest;
import com.cvshealth.digital.microservice.iqe.model.IQEResponse;
import com.cvshealth.digital.microservice.iqe.model.Questions;
import com.cvshealth.digital.microservice.iqe.model.RulesDetails;
import com.cvshealth.digital.microservice.iqe.service.IQEService;
import com.cvshealth.digital.microservice.iqe.testdata.TestDataBuilder;
import com.cvshealth.digital.microservice.iqe.utils.LoggingUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IQEControllerUnitTest {
    
    private WebTestClient webTestClient;
    
    @Mock
    private IQEService iqeService;
    
    @Mock
    private LoggingUtils loggingUtils;
    
    @Mock
    private Map<String, String> errorMessages;
    
    @InjectMocks
    private IQEController iqeController;
    
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        lenient().when(errorMessages.get(any(String.class))).thenReturn("Internal Server Error");
        webTestClient = WebTestClient.bindToController(iqeController).build();
        objectMapper = new ObjectMapper();
    }
    
    @Test
    void shouldGetQuestionnaireRulesSuccessfully() throws Exception {
        RulesDetails request = TestDataBuilder.createTestRulesDetails();
        Questions response = new Questions();
        Map<String, String> headers = new HashMap<>();
        Map<String, Object> reqHeaders = new HashMap<>();
        
        when(iqeService.getRuleDetails(any(RulesDetails.class), any(Map.class), any(Map.class)))
                .thenReturn(Mono.just(response));
        
        webTestClient.post()
                .uri("/schedule/iqe/v1/getquestionnairerules")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }
    
    @Test
    void shouldHandleDynamicFlowConditionEvaluation() throws Exception {
        RulesDetails request = TestDataBuilder.createTestRulesDetails();
        QuestionareRequest response = new QuestionareRequest();
        
        when(iqeService.questionnaireByFlowAndCondition(any(RulesDetails.class), any(QuestionareRequest.class), any(Map.class)))
                .thenReturn(Mono.just(response));
        
        webTestClient.post()
                .uri("/schedule/iqe/v1/questionnaires/dynamic-flow-condition-evaluation")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }
    
    @Test
    void shouldGetQuestionnaireByActionId() throws Exception {
        QuestionareRequest response = new QuestionareRequest();
        
        when(iqeService.questionnaireByActionId(any(String.class), any(QuestionareRequest.class)))
                .thenReturn(Mono.just(response));
        
        webTestClient.get()
                .uri("/schedule/iqe/v1/questionnaire/{actionId}", "test-action-id")
                .exchange()
                .expectStatus().isOk();
    }
    
    @Test
    void shouldCreateQuestionnaireSuccessfully() throws Exception {
        QuestionareRequest request = TestDataBuilder.createTestQuestionareRequest();
        IQEResponse response = new IQEResponse();
        response.setStatusCode("SUCCESS");
        
        when(iqeService.processQuestionnaire(any(QuestionareRequest.class), any(IQEResponse.class), any(Map.class), any(Map.class)))
                .thenReturn(Mono.just(response));
        
        webTestClient.post()
                .uri("/schedule/iqe/v1/questionnaire")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }
    
    @Test
    void shouldGetAllRulesSuccessfully() throws Exception {
        QuestionareRequest response = new QuestionareRequest();
        response.setActiveRules(new java.util.ArrayList<>());
        response.setInactiveRules(new java.util.ArrayList<>());
        
        when(iqeService.rules())
                .thenReturn(Mono.just(response));
        
        webTestClient.get()
                .uri("/schedule/iqe/v1/rules")
                .exchange()
                .expectStatus().isOk();
    }
    
    @Test
    void shouldDeleteQuestionnaireByActionIdSuccessfully() throws Exception {
        IQEResponse response = new IQEResponse();
        response.setStatusCode("SUCCESS");
        
        when(iqeService.deleteQuestionnaireByActionId(any(String.class)))
                .thenReturn(Mono.just(response));
        
        webTestClient.delete()
                .uri("/schedule/iqe/v1/questionnaire/{actionId}", "test-action-id")
                .exchange()
                .expectStatus().isOk();
    }
    
    @Test
    void shouldHandleQuestionnaireByActionAndQuestionId() throws Exception {
        QuestionareRequest response = new QuestionareRequest();
        
        when(iqeService.questionnaireByActionAndQuestionId(any(String.class), any(String.class), any(Map.class)))
                .thenReturn(Mono.just(response));
        
        webTestClient.get()
                .uri("/schedule/iqe/v1/questionnaire/{actionId}/{questionId}", "test-action-id", "test-question-id")
                .exchange()
                .expectStatus().isOk();
    }
    
    @Test
    void shouldHandleQuestionnaireByFlow() throws Exception {
        RulesDetails request = TestDataBuilder.createTestRulesDetails();
        QuestionareRequest response = new QuestionareRequest();
        
        when(iqeService.questionnaireByFlow(any(RulesDetails.class), any(QuestionareRequest.class), any(Map.class)))
                .thenReturn(Mono.just(response));
        
        webTestClient.post()
                .uri("/schedule/iqe/v1/questionnaire/evaluation-by-flow")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }
    
    @Test
    void shouldHandleQuestionsByActionAndQuestionId() throws Exception {
        RelatedQuestionsRequest request = new RelatedQuestionsRequest();
        com.cvshealth.digital.microservice.iqe.dto.Questions question = TestDataBuilder.createTestQuestionDto();
        request.setRelatedQuestions(List.of(question));
        QuestionareRequest response = new QuestionareRequest();
        
        when(iqeService.getQuestionsByRelatedQuestionsList(any(RelatedQuestionsRequest.class), any(QuestionareRequest.class), any(Map.class)))
                .thenReturn(Mono.just(response));
        
        webTestClient.post()
                .uri("/schedule/iqe/v1/questionnaire/related-questions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }
    
    @Test
    void shouldHandleInvalidRequestBody() throws Exception {
        webTestClient.post()
                .uri("/schedule/iqe/v1/getquestionnairerules")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("invalid json")
                .exchange()
                .expectStatus().is4xxClientError();
    }
    
    @Test
    void shouldHandleEmptyRequestBody() throws Exception {
        IQEResponse response = new IQEResponse();
        response.setStatusCode("SUCCESS");
        
        when(iqeService.processQuestionnaire(any(QuestionareRequest.class), any(IQEResponse.class), any(Map.class), any(Map.class)))
                .thenReturn(Mono.just(response));
        
        webTestClient.post()
                .uri("/schedule/iqe/v1/questionnaire")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isOk();
    }
    
    @Test
    void shouldHandleInvalidPathVariable() throws Exception {
        webTestClient.get()
                .uri("/schedule/iqe/v1/questionnaire/{actionId}/{questionId}", "", "test-question-id")
                .exchange()
                .expectStatus().is4xxClientError();
    }
    
    @Test
    void shouldHandleNullRequestBody() throws Exception {
        webTestClient.post()
                .uri("/schedule/iqe/v1/getquestionnairerules")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is4xxClientError();
    }
    
    @Test
    void shouldHandleMissingContentType() throws Exception {
        RulesDetails request = TestDataBuilder.createTestRulesDetails();
        
        webTestClient.post()
                .uri("/schedule/iqe/v1/getquestionnairerules")
                .bodyValue(objectMapper.writeValueAsString(request))
                .exchange()
                .expectStatus().is4xxClientError();
    }
    
    @Test
    void shouldHandleInvalidHttpMethod() throws Exception {
        webTestClient.put()
                .uri("/schedule/iqe/v1/getquestionnairerules")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isEqualTo(405);
    }
    
    @Test
    void shouldHandleGetRequestWithoutPathVariable() throws Exception {
        webTestClient.get()
                .uri("/schedule/iqe/v1/questionnaire/")
                .exchange()
                .expectStatus().is4xxClientError();
    }
    
    @Test
    void shouldHandleDeleteWithInvalidActionId() throws Exception {
        IQEResponse response = new IQEResponse();
        response.setStatusCode("FAILURE");
        response.setStatusDescription("Invalid action ID");
        
        when(iqeService.deleteQuestionnaireByActionId(any(String.class)))
                .thenReturn(Mono.just(response));
        
        webTestClient.delete()
                .uri("/schedule/iqe/v1/questionnaire/{actionId}", "invalid-id")
                .header("Authorization", "Bearer token")
                .header("user_id", "test-user")
                .header("source", "test-source")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.statusCode").isEqualTo("FAILURE")
                .jsonPath("$.statusDescription").isEqualTo("Invalid action ID");
    }
    
    @Test
    void shouldHandleCreateQuestionnaireWithComplexData() throws Exception {
        QuestionareRequest request = TestDataBuilder.createTestQuestionareRequest();
        
        com.cvshealth.digital.microservice.iqe.dto.Questions question = TestDataBuilder.createTestQuestionDto();
        com.cvshealth.digital.microservice.iqe.dto.AnswerOptions answerOption = TestDataBuilder.createTestAnswerOptionDTO();
        com.cvshealth.digital.microservice.iqe.dto.Questions nestedQuestion = TestDataBuilder.createTestQuestionDto();
        nestedQuestion.setQuestionId("nested-question");
        answerOption.setRelatedQuestions(List.of(nestedQuestion));
        question.setAnswerOptions(List.of(answerOption));
        request.setQuestions(List.of(question));
        
        IQEResponse response = new IQEResponse();
        response.setStatusCode("SUCCESS");
        
        when(iqeService.processQuestionnaire(any(QuestionareRequest.class), any(IQEResponse.class), any(Map.class), any(Map.class)))
                .thenReturn(Mono.just(response));
        
        webTestClient.post()
                .uri("/schedule/iqe/v1/questionnaire")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .header("Authorization", "Bearer token")
                .header("user_id", "test-user")
                .header("source", "test-source")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.statusCode").isEqualTo("SUCCESS");
    }
    
    @Test
    void shouldHandleGetQuestionnaireByActionIdWithHeaders() throws Exception {
        QuestionareRequest response = new QuestionareRequest();
        
        when(iqeService.questionnaireByActionId(any(String.class), any(QuestionareRequest.class)))
                .thenReturn(Mono.just(response));
        
        webTestClient.get()
                .uri("/schedule/iqe/v1/questionnaire/{actionId}", "test-action-id")
                .header("Authorization", "Bearer token")
                .header("User-Agent", "Test-Agent")
                .exchange()
                .expectStatus().isOk();
    }
    
    @Test
    void shouldHandleDynamicFlowConditionEvaluationWithComplexRules() throws Exception {
        RulesDetails request = TestDataBuilder.createTestRulesDetails();
        request.setRequiredQuestionnaireContext("complex-context");
        request.setFlow("complex-flow");
        
        QuestionareRequest response = new QuestionareRequest();
        
        when(iqeService.questionnaireByFlowAndCondition(any(RulesDetails.class), any(QuestionareRequest.class), any(Map.class)))
                .thenReturn(Mono.just(response));
        
        webTestClient.post()
                .uri("/schedule/iqe/v1/questionnaires/dynamic-flow-condition-evaluation")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .header("X-Request-ID", "test-request-id")
                .exchange()
                .expectStatus().isOk();
    }
    
    @Test
    void shouldHandleRelatedQuestionsWithEmptyList() throws Exception {
        RelatedQuestionsRequest request = new RelatedQuestionsRequest();
        request.setRelatedQuestions(new java.util.ArrayList<>());
        
        QuestionareRequest response = new QuestionareRequest();
        
        when(iqeService.getQuestionsByRelatedQuestionsList(any(RelatedQuestionsRequest.class), any(QuestionareRequest.class), any(Map.class)))
                .thenReturn(Mono.just(response));
        
        webTestClient.post()
                .uri("/schedule/iqe/v1/questionnaire/related-questions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }
    
    @Test
    void shouldHandleQuestionnaireByFlowWithNullFlow() throws Exception {
        RulesDetails request = TestDataBuilder.createTestRulesDetails();
        request.setFlow(null);
        
        webTestClient.post()
                .uri("/schedule/iqe/v1/questionnaire/evaluation-by-flow")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }
    
    @Test
    void shouldHandleSpecialCharactersInPathVariables() throws Exception {
        QuestionareRequest response = new QuestionareRequest();
        
        when(iqeService.questionnaireByActionAndQuestionId(any(String.class), any(String.class), any(Map.class)))
                .thenReturn(Mono.just(response));
        
        webTestClient.get()
                .uri("/schedule/iqe/v1/questionnaire/{actionId}/{questionId}", 
                        "action-with-special-chars-123", "question-with-dashes-456")
                .exchange()
                .expectStatus().isOk();
    }
    
    @Test
    void shouldHandleServiceErrorInGetQuestionnaireRules() throws Exception {
        RulesDetails request = TestDataBuilder.createTestRulesDetails();
        
        when(iqeService.getRuleDetails(any(RulesDetails.class), any(Map.class), any(Map.class)))
                .thenReturn(Mono.error(new RuntimeException("Database connection failed")));
        
        webTestClient.post()
                .uri("/schedule/iqe/v1/getquestionnairerules")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError();
    }
    
    @Test
    void shouldHandleServiceErrorInCreateQuestionnaire() throws Exception {
        QuestionareRequest request = TestDataBuilder.createTestQuestionareRequest();
        
        when(iqeService.processQuestionnaire(any(QuestionareRequest.class), any(IQEResponse.class), any(Map.class), any(Map.class)))
                .thenReturn(Mono.error(new RuntimeException("Processing failed")));
        
        webTestClient.post()
                .uri("/schedule/iqe/v1/questionnaire")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError();
    }
    
    @Test
    void shouldHandleServiceErrorInGetAllRules() throws Exception {
        when(iqeService.rules())
                .thenReturn(Mono.error(new RuntimeException("Repository error")));
        
        webTestClient.get()
                .uri("/schedule/iqe/v1/rules")
                .exchange()
                .expectStatus().is5xxServerError();
    }
    
    @Test
    void shouldHandleServiceErrorInQuestionnaireByActionId() throws Exception {
        when(iqeService.questionnaireByActionId(any(String.class), any(QuestionareRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("Action not found")));
        
        webTestClient.get()
                .uri("/schedule/iqe/v1/questionnaire/{actionId}", "test-action-id")
                .exchange()
                .expectStatus().is5xxServerError();
    }
    
    @Test
    void shouldHandleServiceErrorInDeleteQuestionnaire() throws Exception {
        when(iqeService.deleteQuestionnaireByActionId(any(String.class)))
                .thenReturn(Mono.error(new RuntimeException("Delete operation failed")));
        
        webTestClient.delete()
                .uri("/schedule/iqe/v1/questionnaire/{actionId}", "test-action-id")
                .exchange()
                .expectStatus().is5xxServerError();
    }
    
    @Test
    void shouldHandleServiceErrorInDynamicFlowConditionEvaluation() throws Exception {
        RulesDetails request = TestDataBuilder.createTestRulesDetails();
        
        when(iqeService.questionnaireByFlowAndCondition(any(RulesDetails.class), any(QuestionareRequest.class), any(Map.class)))
                .thenReturn(Mono.error(new RuntimeException("Flow evaluation failed")));
        
        webTestClient.post()
                .uri("/schedule/iqe/v1/questionnaires/dynamic-flow-condition-evaluation")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError();
    }
    
    @Test
    void shouldHandleServiceErrorInQuestionnaireByActionAndQuestionId() throws Exception {
        when(iqeService.questionnaireByActionAndQuestionId(any(String.class), any(String.class), any(Map.class)))
                .thenReturn(Mono.error(new RuntimeException("Question not found")));
        
        webTestClient.get()
                .uri("/schedule/iqe/v1/questionnaire/{actionId}/{questionId}", "test-action-id", "test-question-id")
                .exchange()
                .expectStatus().is5xxServerError();
    }
    
    @Test
    void shouldHandleServiceErrorInQuestionnaireByFlow() throws Exception {
        RulesDetails request = TestDataBuilder.createTestRulesDetails();
        
        when(iqeService.questionnaireByFlow(any(RulesDetails.class), any(QuestionareRequest.class), any(Map.class)))
                .thenReturn(Mono.error(new RuntimeException("Flow processing failed")));
        
        webTestClient.post()
                .uri("/schedule/iqe/v1/questionnaire/evaluation-by-flow")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError();
    }
    
    @Test
    void shouldHandleServiceErrorInRelatedQuestions() throws Exception {
        RelatedQuestionsRequest request = new RelatedQuestionsRequest();
        request.setRelatedQuestions(List.of(TestDataBuilder.createTestQuestionDto()));
        
        when(iqeService.getQuestionsByRelatedQuestionsList(any(RelatedQuestionsRequest.class), any(QuestionareRequest.class), any(Map.class)))
                .thenReturn(Mono.error(new RuntimeException("Related questions processing failed")));
        
        webTestClient.post()
                .uri("/schedule/iqe/v1/questionnaire/related-questions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
