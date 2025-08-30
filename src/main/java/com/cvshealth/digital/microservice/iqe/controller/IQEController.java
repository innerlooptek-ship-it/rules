package com.cvshealth.digital.microservice.iqe.controller;


import com.cvshealth.digital.microservice.iqe.dto.QuestionareRequest;
import com.cvshealth.digital.microservice.iqe.dto.RelatedQuestionsRequest;
import com.cvshealth.digital.microservice.iqe.exception.CvsException;
import com.cvshealth.digital.microservice.iqe.model.IQEResponse;
import com.cvshealth.digital.microservice.iqe.model.Questions;
import com.cvshealth.digital.microservice.iqe.model.RulesDetails;
import com.cvshealth.digital.microservice.iqe.service.IQEService;
import com.cvshealth.digital.microservice.iqe.service.SimplifiedIQEService;
import com.cvshealth.digital.microservice.iqe.dto.IQEMcCoreQuestionnarieRequest;
import com.cvshealth.digital.microservice.iqe.utils.LoggingUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.cvshealth.digital.microservice.iqe.constants.SchedulingConstants.*;


@RestController
@RequestMapping("/schedule/iqe/v1/")
@Validated
@Slf4j
@CrossOrigin
public class IQEController {

    @Autowired(required = false)
    private IQEService iqeService;
    
    private final SimplifiedIQEService simplifiedIQEService;
    private final LoggingUtils loggingUtils;
    private final Map<String, String> errorMessages;
    
    public IQEController(SimplifiedIQEService simplifiedIQEService, LoggingUtils loggingUtils, Map<String, String> errorMessages) {
        this.simplifiedIQEService = simplifiedIQEService;
        this.loggingUtils = loggingUtils;
        this.errorMessages = errorMessages;
    }

    @Operation(summary = "Gets the IQE Questionnaires", description = "This service is used to get IQE Questionnaire")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Retrieve IQE Questions",
                            content  = {
                                    @Content(mediaType = "application/json",examples = {@ExampleObject(name=SUCCESS_RESPONSE,
                                            summary = SUCCESS_MSG,
                                            value = SUCCESS_RESPONSE_GET_QUESTIONNAIRE_RULES)}) })
            })
    @PostMapping("/getquestionnairerules")
    public Mono<Questions> getQuestionnaire(@Valid  @RequestBody RulesDetails rulesDetails, @RequestHeader Map<String, String> headers) {
         long lStart = System.currentTimeMillis();

         Map<String, Object> eventMap =
                 LoggingUtils.populateEventMap(
                         CLASS_NAME,
                         "getIQEQuestions",
                         CLASS_NAME,
                         "This service is used to get getIQEQuestions",
                         headers);
         loggingUtils.entryEventLogging(log, eventMap);


        return iqeService.getRuleDetails(rulesDetails, headers,eventMap).doFinally(response -> {
            long endTime = System.currentTimeMillis();
            eventMap.put(RESP_TIME,  endTime - lStart);
            loggingUtils.exitEventLogging(log, eventMap);
        });
    }

    @Operation(summary = "Creates New IQE Questionnaires", description = "This service is used to create new IQE Questionnaire")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Create IQE Questions",
                            content  = {
                                    @Content(mediaType = "application/json",examples = {@ExampleObject(name=SUCCESS_RESPONSE,
                                            summary = SUCCESS_MSG,
                                            value = SUCCESS_RESPONSE_CREATE_QUESTIONNAIRE)}) }),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Create IQE Questions",
                            content  = {
                                    @Content(mediaType = "application/json",examples = {@ExampleObject(name=BAD_REQUEST_MESSAGE,
                                            summary = BAD_REQUEST_MESSAGE,
                                            value = FAILURE_RESPONSE_CREATE_QUESTIONNAIRE)}) })
            })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Create IQE Questionnaire", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
        @ExampleObject( value = CREATE_QUESTIONNAIRE_REQUEST)}))
    @PostMapping("/questionnaire")
    public Mono<IQEResponse> createQuestionnaire(@RequestBody QuestionareRequest questionareRequest,
                                                 @RequestHeader Map<String, String> headers,
                                                 @RequestHeader Map<String, String> reqHdrMap) {

        long lStart = System.currentTimeMillis();

        Map<String, Object> eventMap =
                LoggingUtils.populateEventMap(
                        CLASS_NAME,
                        "createQuestionnaire",
                        CLASS_NAME,
                        "This service is used to create new question",
                        headers);
        loggingUtils.entryEventLogging(log, eventMap);

        IQEResponse iqeResponse = new IQEResponse();

        return Mono.deferContextual(
                ctx ->
                        iqeService.processQuestionnaire(questionareRequest, iqeResponse, reqHdrMap, eventMap)
                                .doFinally(response -> {
                                    long endTime = System.currentTimeMillis();
                                    eventMap.put(RESP_TIME, endTime - lStart);
                                    loggingUtils.exitEventLogging(log, eventMap);
                                })
        );
    }

    @Operation(summary = "Get All Rules By Flows", description = "This service is used to get All Rules By Flows")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Get IQE Rules",
                            content  = {
                                    @Content(mediaType = "application/json",examples = {@ExampleObject(name=SUCCESS_RESPONSE,
                                            summary = SUCCESS_MSG,
                                            value = SUCCESS_RESPONSE_GET_RULES)}) })
            })
    @GetMapping("/rules")
    public Mono<QuestionareRequest> getAllRules(
            @RequestHeader Map<String, String> headers,
            @RequestHeader Map<String, String> reqHdrMap) {

        long lStart = System.currentTimeMillis();

        Map<String, Object> eventMap =
                LoggingUtils.populateEventMap(
                        CLASS_NAME,
                        "getAllRules",
                        CLASS_NAME,
                        "This service is used to retrieve all the existing rules",
                        headers);
        loggingUtils.entryEventLogging(log, eventMap);

        return Mono.deferContextual(
                ctx ->
                        iqeService.rules()
                                .map(questionareRequest -> {
                                    if (questionareRequest.getActiveRules().isEmpty() && questionareRequest.getInactiveRules().isEmpty()) {
                                        questionareRequest.setErrorDescription("No Rules available");
                                    }
                                    return questionareRequest;
                                })
                                .onErrorResume(error -> {
                                    if (error instanceof CvsException) {
                                        return Mono.error(error);
                                    }
                                    return Mono.error(
                                            new CvsException(
                                                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                    ERROR_INTERNAL_SERVER_ERROR,
                                                    errorMessages.get(INTERNAL_SERVER_ERROR_MESSAGE),
                                                    errorMessages.get(INTERNAL_SERVER_ERROR_MESSAGE),
                                                    error.getMessage()
                                            )
                                    );
                                })
                                .doFinally(response -> {
                                    long endTime = System.currentTimeMillis();
                                    eventMap.put(RESP_TIME, endTime - lStart);
                                    loggingUtils.exitEventLogging(log, eventMap);
                                })
        );
    }

    @Operation(summary = "Get the question based on action Id", description = "This service is used to Get the question based on action Id")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Gets Question based on action Id",
                            content  = {
                                    @Content(mediaType = "application/json",examples = {@ExampleObject(name=SUCCESS_RESPONSE,
                                            summary = SUCCESS_MSG,
                                            value = SUCCESS_RESPONSE_GET_BY_ACTION_ID)}) }),
                    @ApiResponse(
                            responseCode = "200",
                            description = "Create IQE Questions",
                            content  = {
                                    @Content(mediaType = "application/json",examples = {@ExampleObject(name=REQUEST_NOT_FOUND,
                                            summary = REQUEST_NOT_FOUND,
                                            value = FAILURE_RESPONSE_GET_QUESTION_BY_ACTION_ID_QUESTION_ID)}) })

            })
    @GetMapping("/questionnaire/{actionId}")
    public Mono<QuestionareRequest> questionnaireByActionId(@PathVariable("actionId") String actionId,
                                                            @RequestHeader Map<String, String> headers,
                                                            @RequestHeader Map<String, String> reqHdrMap) {

        long lStart = System.currentTimeMillis();

        Map<String, Object> eventMap =
                LoggingUtils.populateEventMap(
                        CLASS_NAME,
                        "questionnaireByActionId",
                        CLASS_NAME,
                        "This service is used to retrieve questions by actionId",
                        headers);
        loggingUtils.entryEventLogging(log, eventMap);

        QuestionareRequest iqeOutPut = new QuestionareRequest();

        return Mono.deferContextual(
                ctx ->
                        iqeService.questionnaireByActionId(actionId, iqeOutPut)
                                .onErrorResume(error -> {
                                    if (error instanceof CvsException) {
                                        return Mono.error(error);
                                    }
                                    return Mono.error(
                                            new CvsException(
                                                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                    ERROR_INTERNAL_SERVER_ERROR,
                                                    errorMessages.get(INTERNAL_SERVER_ERROR_MESSAGE),
                                                    errorMessages.get(INTERNAL_SERVER_ERROR_MESSAGE),
                                                    error.getMessage()
                                            )
                                    );
                                })
                                .doFinally(response -> {
                                    long endTime = System.currentTimeMillis();
                                    eventMap.put(RESP_TIME, endTime - lStart);
                                    loggingUtils.exitEventLogging(log, eventMap);
                                })
        );
    }

    @Operation(summary = "Deletes the question based on action Id", description = "This service is used to Deletes the question based on action Id")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Deletes Question based on action Id",
                            content  = {
                                    @Content(mediaType = "application/json",examples = {@ExampleObject(name=SUCCESS_RESPONSE,
                                            summary = SUCCESS_MSG,
                                            value = SUCCESS_RESPONSE_DELETE_BY_ACTION_ID)}) }),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Create IQE Questions",
                            content  = {
                                    @Content(mediaType = "application/json",examples = {@ExampleObject(name=REQUEST_NOT_FOUND,
                                            summary = REQUEST_NOT_FOUND,
                                            value = FAILURE_RESPONSE_DELETE_QUESTION_BY_ACTION_ID)}) })

            })
    @DeleteMapping("/questionnaire/{actionId}")
    public Mono<IQEResponse> deleteQuestionnaireByActionId(@PathVariable("actionId") String actionId,
                                                           @RequestHeader Map<String, String> headers,
                                                           @RequestHeader Map<String, String> reqHdrMap) {

        long lStart = System.currentTimeMillis();

        Map<String, Object> eventMap =
                LoggingUtils.populateEventMap(
                        CLASS_NAME,
                        "deleteQuestionnaireByActionId",
                        CLASS_NAME,
                        "This service is used to delete questionnaires by actionId",
                        headers);
        loggingUtils.entryEventLogging(log, eventMap);

        return Mono.deferContextual(
                ctx ->
                        iqeService.deleteQuestionnaireByActionId(actionId)
                                .onErrorResume(error -> {
                                    if (error instanceof CvsException) {
                                        return Mono.error(error);
                                    }
                                    return Mono.error(
                                            new CvsException(
                                                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                    ERROR_INTERNAL_SERVER_ERROR,
                                                    errorMessages.get(INTERNAL_SERVER_ERROR_MESSAGE),
                                                    errorMessages.get(INTERNAL_SERVER_ERROR_MESSAGE),
                                                    error.getMessage()
                                            )
                                    );
                                })
                                .doFinally(response -> {
                                    long endTime = System.currentTimeMillis();
                                    eventMap.put(RESP_TIME, endTime - lStart);
                                    loggingUtils.exitEventLogging(log, eventMap);
                                })
        );
    }

    @Operation(summary = "Get IQE Questionnaire by flow and condition", description = "This service is used to Get IQE Questionnaire by flow and condition")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Gets IQE Questions",
                            content  = {
                                    @Content(mediaType = "application/json",examples = {@ExampleObject(name=SUCCESS_RESPONSE,
                                            summary = SUCCESS_MSG,
                                            value = SUCCESS_RESPONSE_DYNAMIC_FLOW)}) })

            })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Gets IQE Questionnaire", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
            @ExampleObject( value = DYNAMIC_FLOW_REQUEST)}))
    @PostMapping("/questionnaires/dynamic-flow-condition-evaluation")
    public Mono<QuestionareRequest> questionnaireByFlowAndCondition(@Valid  @RequestBody RulesDetails rulesDetails, @RequestHeader Map<String, String> headers){

        long lStart = System.currentTimeMillis();

        Map<String, Object> eventMap =
                LoggingUtils.populateEventMap(
                        CLASS_NAME,
                        "dynamic-flow-condition-evaluation",
                        CLASS_NAME,
                        "This service is used to retrieve questionare by flow and condition",
                        headers);
        loggingUtils.entryEventLogging(log, eventMap);

        QuestionareRequest iqeOutPut=new QuestionareRequest();

        return Mono.deferContextual(
                ctx -> {
                    if (iqeService != null) {
                        return iqeService.questionnaireByFlowAndCondition(rulesDetails, iqeOutPut, headers);
                    } else {
                        IQEMcCoreQuestionnarieRequest request = IQEMcCoreQuestionnarieRequest.builder()
                                .requiredQuestionnaireContext(rulesDetails.getRequiredQuestionnaireContext())
                                .flow(rulesDetails.getFlow())
                                .reasonId(rulesDetails.getReasonId())
                                .reasonMappingId(rulesDetails.getReasonMappingId())
                                .build();
                        
                        return simplifiedIQEService.dynamicFlowConditionEvaluation(request)
                                .map(response -> {
                                    QuestionareRequest result = new QuestionareRequest();
                                    return result;
                                });
                    }
                })
                .onErrorResume(error -> {
                    if (error instanceof CvsException) {
                        return Mono.error(error);
                    }
                    return Mono.error(
                            new CvsException(
                                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                    ERROR_INTERNAL_SERVER_ERROR,
                                    errorMessages.get(INTERNAL_SERVER_ERROR_MESSAGE),
                                    errorMessages.get(INTERNAL_SERVER_ERROR_MESSAGE),
                                    error.getMessage()
                            )
                    );
                })
                .doFinally(response -> {
                    long endTime = System.currentTimeMillis();
                    if (rulesDetails.getFlow() != null && !rulesDetails.getFlow().isEmpty()) {
                        eventMap.put(FLOW, rulesDetails.getFlow());
                    }
                    eventMap.put(RESP_TIME, endTime - lStart);
                    loggingUtils.exitEventLogging(log, eventMap);
                });
    }

    @Operation(summary = "Get the question based on action Id and Question Id", description = "This service is used to Get the question based on action Id and Question Id")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Gets Question based on action Id and Question Id",
                            content  = {
                                    @Content(mediaType = "application/json",examples = {@ExampleObject(name=SUCCESS_RESPONSE,
                                            summary = SUCCESS_MSG,
                                            value = SUCCESS_RESPONSE_GET_BY_ACTION_ID_AND_QUESTION_ID)}) }),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Create IQE Questions",
                            content  = {
                                    @Content(mediaType = "application/json",examples = {@ExampleObject(name=REQUEST_NOT_FOUND,
                                            summary = REQUEST_NOT_FOUND,
                                            value = FAILURE_RESPONSE_GET_QUESTION_BY_ACTION_ID_QUESTION_ID)}) })

            })
    @GetMapping("/questionnaire/{actionId}/{questionId}")
    public Mono<QuestionareRequest> questionnaireByActionAndQuestionId(@PathVariable("actionId") String actionId,
                                                                       @PathVariable("questionId") String questionId,
                                                                       @RequestHeader Map<String, String> headers) {
        long lStart = System.currentTimeMillis();

        Map<String, Object> eventMap =
                LoggingUtils.populateEventMap(
                        CLASS_NAME,
                        "questionnaireByActionAndQuestionId",
                        CLASS_NAME,
                        "This service is used to retrieve questionare by Action and Question ID",
                        headers);
        loggingUtils.entryEventLogging(log, eventMap);

        return Mono.deferContextual(
                ctx ->
                        iqeService.questionnaireByActionAndQuestionId(actionId, questionId, headers)
                                .onErrorResume(error -> {
                                    if (error instanceof CvsException) {
                                        return Mono.error(error);
                                    }
                                    return Mono.error(
                                            new CvsException(
                                                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                    ERROR_INTERNAL_SERVER_ERROR,
                                                    errorMessages.get(INTERNAL_SERVER_ERROR_MESSAGE),
                                                    errorMessages.get(INTERNAL_SERVER_ERROR_MESSAGE),
                                                    error.getMessage()
                                            )
                                    );
                                })
                                .doFinally(response -> {
                                    long endTime = System.currentTimeMillis();
                                    eventMap.put("respTime", endTime - lStart);
                                    loggingUtils.exitEventLogging(log, eventMap);
                                })
        );
    }

        @Operation(summary = "Get first set of Questions based on flow and condition", description = "This service is used to Get first set of Questions based on flow and condition")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Gets IQE Primary Questions",
                            content  = {
                                    @Content(mediaType = "application/json",examples = {@ExampleObject(name=SUCCESS_RESPONSE,
                                            summary = SUCCESS_MSG,
                                            value = SUCCESS_RESPONSE_EVALUATION_FLOW)}) })

            })
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Gets IQE Questionnaire", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
            @ExampleObject( value = DYNAMIC_FLOW_REQUEST)}))
    @PostMapping("/questionnaire/evaluation-by-flow")
    public Mono<QuestionareRequest> questionnaireByFlow(@Valid @RequestBody RulesDetails rulesDetails, @RequestHeader Map<String, String> headers){

        long lStart = System.currentTimeMillis();

        Map<String, Object> eventMap =
                LoggingUtils.populateEventMap(
                        CLASS_NAME,
                        "evaluation-by-flow",
                        CLASS_NAME,
                        "This service is used to retrieve questionare by Action and Question ID",
                        headers);
        loggingUtils.entryEventLogging(log, eventMap);

        QuestionareRequest iqeOutPut=new QuestionareRequest();

        return Mono.deferContextual(
                ctx ->
                        iqeService.questionnaireByFlow(rulesDetails, iqeOutPut, headers)
                                .onErrorResume(error -> {
                                    if (error instanceof CvsException) {
                                        return Mono.error(error);
                                    }
                                    return Mono.error(
                                            new CvsException(
                                                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                    ERROR_INTERNAL_SERVER_ERROR,
                                                    errorMessages.get(INTERNAL_SERVER_ERROR_MESSAGE),
                                                    errorMessages.get(INTERNAL_SERVER_ERROR_MESSAGE),
                                                    error.getMessage()
                                            )
                                    );
                                })
                                .doFinally(response -> {
                                    long endTime = System.currentTimeMillis();
                                    eventMap.put("respTime", endTime - lStart);
                                    loggingUtils.exitEventLogging(log, eventMap);
                                })
        );
    }

    @Operation(summary = "Get questions by list of action and question id's", description = "This service is used to Get questions by list of action and question id's")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Gets all Questions",
                            content  = {
                                    @Content(mediaType = "application/json",examples = {@ExampleObject(name=SUCCESS_RESPONSE,
                                            summary = SUCCESS_MSG,
                                            value = SUCCESS_RESPONSE_EVALUATION_FLOW)}) })

            })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Gets IQE Questionnaire", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
            @ExampleObject( value = RELATED_QUESTIONS_REQUEST)}))
    @PostMapping("/questionnaire/related-questions")
    public Mono<QuestionareRequest> questionsByActionAndQuestionId(@Valid @RequestBody RelatedQuestionsRequest relatedQuestions, @RequestHeader Map<String, String> headers){

        long lStart = System.currentTimeMillis();

        Map<String, Object> eventMap =
                LoggingUtils.populateEventMap(
                        CLASS_NAME,
                        "evaluation-by-flow",
                        CLASS_NAME,
                        "This service is used to retrieve questionare by Action and Question ID",
                        headers);
        loggingUtils.entryEventLogging(log, eventMap);

        QuestionareRequest iqeOutPut=new QuestionareRequest();

        return Mono.deferContextual(
                ctx ->
                        iqeService.getQuestionsByRelatedQuestionsList(relatedQuestions, iqeOutPut,headers)
                                .onErrorResume(error -> {
                                    if (error instanceof CvsException) {
                                        return Mono.error(error);
                                    }
                                    return Mono.error(
                                            new CvsException(
                                                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                    ERROR_INTERNAL_SERVER_ERROR,
                                                    errorMessages.get(INTERNAL_SERVER_ERROR_MESSAGE),
                                                    errorMessages.get(INTERNAL_SERVER_ERROR_MESSAGE),
                                                    error.getMessage()
                                            )
                                    );
                                })
                                .doFinally(response -> {
                                    long endTime = System.currentTimeMillis();
                                    eventMap.put("respTime", endTime - lStart);
                                    loggingUtils.exitEventLogging(log, eventMap);
                                })
        );
    }

    @Operation(summary = "Create IQE Questionnaire using simplified Redis caching", description = "This API uses complete dataset cached in Redis for all questionnaire operations")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Success Response",
                            content = {
                                    @Content(mediaType = "application/json", examples = {@ExampleObject(name = SUCCESS_RESPONSE,
                                            summary = SUCCESS_MSG,
                                            value = SUCCESS_RESPONSE_CREATE_QUESTIONNAIRE)})
                            }),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad Request",
                            content = {
                                    @Content(mediaType = "application/json", examples = {@ExampleObject(name = BAD_REQUEST_MESSAGE,
                                            summary = BAD_REQUEST_MESSAGE,
                                            value = FAILURE_RESPONSE_CREATE_QUESTIONNAIRE)})
                            })
            })
    @PostMapping("/questionnaires/simplified-dynamic-flow-condition-evaluation")
    public Mono<QuestionareRequest> createQuestionnaireSimplified(@RequestBody IQEMcCoreQuestionnarieRequest questionareRequest,
                                                                  @RequestHeader Map<String, String> headers) {
        long lStart = System.currentTimeMillis();

        Map<String, Object> eventMap =
                LoggingUtils.populateEventMap(
                        CLASS_NAME,
                        "createQuestionnaireSimplified",
                        CLASS_NAME,
                        "This service uses simplified Redis caching for questionnaire creation",
                        headers);
        loggingUtils.entryEventLogging(log, eventMap);

        return simplifiedIQEService.dynamicFlowConditionEvaluation(questionareRequest)
                .doFinally(response -> {
                    long endTime = System.currentTimeMillis();
                    eventMap.put(RESP_TIME, endTime - lStart);
                    loggingUtils.exitEventLogging(log, eventMap);
                });
    }
}
