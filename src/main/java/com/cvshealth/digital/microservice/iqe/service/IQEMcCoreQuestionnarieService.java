package com.cvshealth.digital.microservice.iqe.service;


import com.cvshealth.digital.microservice.iqe.config.MessageConfig;


import com.cvshealth.digital.microservice.iqe.dto.IQEMcCoreQuestionnarieRequest;
import com.cvshealth.digital.microservice.iqe.dto.IQEMcCoreQuestionnarieResponse;
import com.cvshealth.digital.microservice.iqe.exception.CvsException;
import com.cvshealth.digital.microservice.iqe.udt.LoggingUtils;
import com.cvshealth.digital.microservice.iqe.udt.SchedulingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.cvshealth.digital.microservice.iqe.udt.SchedulingConstants.IQE_MC_CORE_OPS_NAME;
import static com.cvshealth.digital.microservice.iqe.udt.SchedulingConstants.IQE_OPS_NAME;


@Service
public class IQEMcCoreQuestionnarieService {
    private static final Logger logger = LoggerFactory.getLogger(IQEMcCoreQuestionnarieService.class);
    @Autowired
    @Qualifier("iQeMcCoreQuestionnarie")
    WebClient iQEWebClient;

    @Autowired
    @Qualifier("dynamicFlowConditionEvaluation")
    WebClient iQEGetWebClient;

    @Autowired
    MessageConfig messagesConfig;

    @Autowired
    LoggingUtils loggingUtils;
//    @CircuitBreaker(name = "retryGetIQEMcCoreQuestions", fallbackMethod = "getIQEMcCoreQuestionsFallback")
    public Mono<IQEMcCoreQuestionnarieResponse> getIQEMcCoreQuestions(IQEMcCoreQuestionnarieRequest iqeMcCoreQuestionnarieRequest, Map<String,Object> tags, Map<String,String> headerMap ) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> eventMap = (tags.get("eventMap")!=null)?(new HashMap<>( (Map<String, Object>) tags.get("eventMap"))):new LinkedHashMap<>();
        eventMap.put(SchedulingConstants.OPNAME, IQE_MC_CORE_OPS_NAME);
        Map<String, String> messageConfigs = messagesConfig.getMessages();

        HttpHeaders httpHeaders =new HttpHeaders();
        httpHeaders.add( SchedulingConstants.CONST_CATEGORY,headerMap.get(SchedulingConstants.CONST_CATEGORY));
        httpHeaders.add( SchedulingConstants.CONST_X_GRID,headerMap.get(SchedulingConstants.CONST_X_GRID));
        httpHeaders.add( SchedulingConstants.CONST_EXP_ID,headerMap.get(SchedulingConstants.CONST_EXP_ID));
        httpHeaders.add( SchedulingConstants.CONST_CLIENTREFID,headerMap.get(SchedulingConstants.CONST_CLIENTREFID));
        return iQEWebClient.method(HttpMethod.POST)
                .headers(h -> h.addAll(httpHeaders))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(iqeMcCoreQuestionnarieRequest))
                .retrieve()
                .onStatus(HttpStatus.BAD_REQUEST::equals, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(error ->{
                                    eventMap.put(SchedulingConstants.STATUS_CDE, SchedulingConstants.ERROR_BAD_REQUEST);
                                    eventMap.put(SchedulingConstants.STATUS_MESSAGE, messageConfigs.get("common.BAD_REQUEST"));
                                    eventMap.put(SchedulingConstants.STATUS_DESC, error);
                                    eventMap.put(SchedulingConstants.HTTP_STATUS_CDE, HttpStatus.BAD_REQUEST.value());
                                    loggingUtils.errorEventLogging(logger, eventMap);
                                    tags.put(SchedulingConstants.IQE_MC_CORE_RESP_TIME, System.currentTimeMillis() - startTime);
                                    tags.put(IQE_MC_CORE_OPS_NAME + "_" + SchedulingConstants.STATUS_CDE, SchedulingConstants.ERROR_BAD_REQUEST);
                                    tags.put(IQE_MC_CORE_OPS_NAME + "_" + SchedulingConstants.STATUS_MESSAGE, messageConfigs.get("common.BAD_REQUEST"));
                                    tags.put("getIQEMcCoreQuestions_Response", error);
                                    return Mono.error(new CvsException(HttpStatus.BAD_REQUEST.value(),
                                            "INVALID_IQE_REQUEST", messageConfigs.get("upcomingAppts.INVALID_IQE_REQUEST"), error,"INVALID_IQE_REQUEST"));
                                })
                )
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(error -> {
                                    eventMap.put(SchedulingConstants.STATUS_CDE, SchedulingConstants.ERROR_INTERNAL_SERVER_ERROR);
                                    eventMap.put(SchedulingConstants.STATUS_MESSAGE, messagesConfig.getMessages().get("common.INTERNAL_SERVER_ERROR"));
                                    eventMap.put(SchedulingConstants.STATUS_DESC, error);
                                    eventMap.put(SchedulingConstants.HTTP_STATUS_CDE, HttpStatus.INTERNAL_SERVER_ERROR.value());
                                    loggingUtils.errorEventLogging(logger, eventMap);
                                    tags.put(SchedulingConstants.IQE_MC_CORE_RESP_TIME, System.currentTimeMillis() - startTime);
                                    tags.put(IQE_MC_CORE_OPS_NAME + "_" + SchedulingConstants.STATUS_CDE, SchedulingConstants.ERROR_INTERNAL_SERVER_ERROR);
                                    tags.put(IQE_MC_CORE_OPS_NAME + "_" + SchedulingConstants.STATUS_MESSAGE, messagesConfig.getMessages().get("common.INTERNAL_SERVER_ERROR"));
                                    tags.put("getIQEMcCoreQuestions_Response", error);
                                    return Mono.error(new WebClientResponseException(HttpStatus.INTERNAL_SERVER_ERROR.value(), error, null, null, null));
                                })
                )
                .bodyToMono(IQEMcCoreQuestionnarieResponse.class)
                .doOnSuccess(e -> {
                    long endTime = System.currentTimeMillis();
                    long elapsedTime = endTime - startTime;
                    tags.put(SchedulingConstants.IQE_MC_CORE_RESP_TIME, elapsedTime);
                    tags.put(IQE_MC_CORE_OPS_NAME + "_" + SchedulingConstants.STATUS_CDE, SchedulingConstants.SUCCESS_CODE);
                    tags.put(IQE_MC_CORE_OPS_NAME + "_" + SchedulingConstants.STATUS_MESSAGE, SchedulingConstants.SUCCESS_MSG);
                    loggingUtils.infoEventLogging(logger, eventMap);

                });


    }


    public Mono<IQEMcCoreQuestionnarieResponse> getIQEGetQuestions(IQEMcCoreQuestionnarieRequest iqeMcCoreQuestionnarieRequest, Map<String,Object> tags, Map<String,String> headerMap ) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> eventMap = (tags.get("eventMap")!=null)?(new HashMap<>( (Map<String, Object>) tags.get("eventMap"))):new LinkedHashMap<>();
        eventMap.put(SchedulingConstants.OPNAME, IQE_OPS_NAME);
        Map<String, String> messageConfigs = messagesConfig.getMessages();

        HttpHeaders httpHeaders =new HttpHeaders();
        httpHeaders.add( SchedulingConstants.CONST_CATEGORY,headerMap.get(SchedulingConstants.CONST_CATEGORY));
        httpHeaders.add( SchedulingConstants.CONST_X_GRID,headerMap.get(SchedulingConstants.CONST_X_GRID));
        httpHeaders.add( SchedulingConstants.CONST_EXP_ID,headerMap.get(SchedulingConstants.CONST_EXP_ID));
        httpHeaders.add( SchedulingConstants.CONST_CLIENTREFID,headerMap.get(SchedulingConstants.CONST_CLIENTREFID));

       return  iQEGetWebClient.method(HttpMethod.POST)
                .headers(h -> h.addAll(httpHeaders))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(iqeMcCoreQuestionnarieRequest))
                .retrieve()

                .onStatus(HttpStatus.BAD_REQUEST::equals, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(error ->{
                                    eventMap.put(SchedulingConstants.STATUS_CDE, SchedulingConstants.ERROR_BAD_REQUEST);
                                    eventMap.put(SchedulingConstants.STATUS_MESSAGE, messageConfigs.get("common.BAD_REQUEST"));
                                    eventMap.put(SchedulingConstants.STATUS_DESC, error);
                                    eventMap.put(SchedulingConstants.HTTP_STATUS_CDE, HttpStatus.BAD_REQUEST.value());
                                    loggingUtils.errorEventLogging(logger, eventMap);
                                    tags.put(SchedulingConstants.IQE_RESP_TIME, System.currentTimeMillis() - startTime);
                                    tags.put(IQE_OPS_NAME + "_" + SchedulingConstants.STATUS_CDE, SchedulingConstants.ERROR_BAD_REQUEST);
                                    tags.put(IQE_OPS_NAME + "_" + SchedulingConstants.STATUS_MESSAGE, messageConfigs.get("common.BAD_REQUEST"));
                                    tags.put("getIQEMcCoreQuestions_Response", error);
                                    return Mono.error(new CvsException(HttpStatus.BAD_REQUEST.value(),
                                            "INVALID_IQE_REQUEST", messageConfigs.get("upcomingAppts.INVALID_IQE_REQUEST"), error,"INVALID_IQE_REQUEST"));
                                })
                )
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(error -> {
                                    eventMap.put(SchedulingConstants.STATUS_CDE, SchedulingConstants.ERROR_INTERNAL_SERVER_ERROR);
                                    eventMap.put(SchedulingConstants.STATUS_MESSAGE, messagesConfig.getMessages().get("common.INTERNAL_SERVER_ERROR"));
                                    eventMap.put(SchedulingConstants.STATUS_DESC, error);
                                    eventMap.put(SchedulingConstants.HTTP_STATUS_CDE, HttpStatus.INTERNAL_SERVER_ERROR.value());
                                    loggingUtils.errorEventLogging(logger, eventMap);
                                    tags.put(SchedulingConstants.IQE_RESP_TIME, System.currentTimeMillis() - startTime);
                                    tags.put(IQE_OPS_NAME + "_" + SchedulingConstants.STATUS_CDE, SchedulingConstants.ERROR_INTERNAL_SERVER_ERROR);
                                    tags.put(IQE_OPS_NAME + "_" + SchedulingConstants.STATUS_MESSAGE, messagesConfig.getMessages().get("common.INTERNAL_SERVER_ERROR"));
                                    tags.put("getIQEMcCoreQuestions_Response", error);
                                    return Mono.error(new WebClientResponseException(HttpStatus.INTERNAL_SERVER_ERROR.value(), error, null, null, null));
                                })
                )
                .bodyToMono(IQEMcCoreQuestionnarieResponse.class)
                .doOnSuccess(e -> {

                    long endTime = System.currentTimeMillis();
                    long elapsedTime = endTime - startTime;
                    tags.put(SchedulingConstants.IQE_RESP_TIME, elapsedTime);
                    tags.put(IQE_OPS_NAME + "_" + SchedulingConstants.STATUS_CDE, SchedulingConstants.SUCCESS_CODE);
                    tags.put(IQE_OPS_NAME + "_" + SchedulingConstants.STATUS_MESSAGE, SchedulingConstants.SUCCESS_MSG);
                    loggingUtils.infoEventLogging(logger, eventMap);
                });

    }




    public Mono<Object> getIQEMcCoreQuestionsFallback(IQEMcCoreQuestionnarieRequest iqeMcCoreQuestionnarieRequest, Map<String,Object> tags, Map<String,String> headerMap, Throwable e) {
        if(e instanceof CvsException) {
            return Mono.error(e);
        }
        tags.put(IQE_MC_CORE_OPS_NAME + "_" + SchedulingConstants.STATUS_CDE, SchedulingConstants.ERROR_INTERNAL_SERVER_ERROR);
        tags.put(IQE_MC_CORE_OPS_NAME + "_" + SchedulingConstants.STATUS_MESSAGE, messagesConfig.getMessages().get("getIQEMcCoreQuestions.GET_IQE_MC_CORE_QUESTIONS_ERROR"));
        tags.put("getIQEMcCoreQuestions_Response", e.getMessage());
        return Mono.error(new CvsException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "GET_IQE_MC_CORE_QUESTIONS_ERROR", messagesConfig.getMessages().get("getIQEMcCoreQuestions.GET_IQE_MC_CORE_QUESTIONS_ERROR"), e.getMessage(), "GET_IQE_MC_CORE_QUESTIONS"));
    }
}