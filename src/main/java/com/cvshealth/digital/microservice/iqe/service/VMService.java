//package com.cvshealth.digital.microservice.iqe.service;
//
//
//import com.cvshealth.digital.microservice.iqe.config.MessageConfig;
//import com.cvshealth.digital.microservice.iqe.config.WebClientHelper;
//import com.cvshealth.digital.microservice.iqe.constants.SchedulingConstants;
//import com.cvshealth.digital.microservice.iqe.dto.SchedulingConsentInput;
//import com.cvshealth.digital.microservice.iqe.dto.VMGetConsentsResponse;
//import com.cvshealth.digital.microservice.iqe.enums.ErrorKey;
//import com.cvshealth.digital.microservice.iqe.model.GetConsentInput;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.Map;
//import reactor.core.scheduler.Schedulers;
//
//import static com.cvshealth.digital.microservice.iqe.constants.SchedulingConstants.*;
//import static com.cvshealth.digital.microservice.iqe.utils.CvsLoggingUtil.logErrorEvent;
//import static com.cvshealth.digital.microservice.iqe.utils.CvsLoggingUtil.logInfoEvent;
//import static com.cvshealth.digital.microservice.iqe.utils.LoggingUtils.populateEventMap;
//
//@Component
//public class VMService  {
//
//    private final WebClient vmGetConsentWebClient;
//    private final WebClient webClientVMSetConsents;
//    private final WebClient webClientVMSetQuestionnaire;
//    private final WebClient webClientVMAppointmentStatus;
//
//    private final CacheUtils cacheUtils;
//    private final ErrorUtils errorUtils;
//    private final WebClientHelper webClientHelper;
//    private final MessageConfig messagesConfig;
//
//    @Autowired
//    public VMService(
//        ErrorUtils errorUtils,
//        CacheUtils cacheUtils,
//        WebClientHelper webClientHelper,
//        @Qualifier("vmGetConsents") WebClient vmGetConsentWebClient,
//        @Qualifier("vmSetConsents") WebClient webClientVMSetConsents,
//        @Qualifier("vmSetQuestionnaire") WebClient webClientVMSetQuestionnaire,
//        @Qualifier("vmAppointmentStatus") WebClient webClientVMAppointmentStatus,
//        MessageConfig messagesConfig
//    ) {
//        this.cacheUtils = cacheUtils;
//        this.errorUtils = errorUtils;
//        this.webClientHelper = webClientHelper;
//        this.vmGetConsentWebClient = vmGetConsentWebClient;
//        this.webClientVMSetConsents = webClientVMSetConsents;
//        this.webClientVMSetQuestionnaire = webClientVMSetQuestionnaire;
//        this.webClientVMAppointmentStatus = webClientVMAppointmentStatus;
//        this.messagesConfig = messagesConfig;
//    }
//
//    @Override
////    @CircuitBreaker(name = "getVMConsents", fallbackMethod = "getVMConsentsFallback")
//////    @Retry(name = "getVMConsents")
//    public Mono<VMGetConsentsResponse> getVMConsents(
//            String id,
//            String idType,
//            GetConsentInput consentInput,
//        Map<String, Object> tags,
//        Map<String, String> headers
//    ) {
//
//        long startTime = System.currentTimeMillis();
//
//        String apiName = "getVMConsents";
//
//        Map<String, Object> eventMap = populateEventMap(
//            this.getClass().getName(),
//            apiName,
//            apiName,
//            apiName,
//            headers
//        );
//
//        VMGetConsentsRequest vmGetConsentsRequest = VMGetConsentsRequest.builder()
//            .id(id)
//            .idType(idType)
//            .appointmentId(consentInput.getAppointmentId())
//            .patientReferenceId(consentInput.getConsentsDataInput().getFirst().getPatientReferenceId())
//            .consentContext(consentInput.getConsentsDataInput().getFirst().getConsentContext())
//            .build();
//
//        return webClientHelper
//            .createWebClient(vmGetConsentWebClient, vmGetConsentsRequest, VMGetConsentsRequest.class, ErrorKey.GET_VM_CONSENTS, headers, eventMap)
//            .onStatus(HttpStatus.NOT_FOUND::equals, webClientHelper.onStatusCvsException(APPOINTMENT_NOT_FOUND, ErrorKey.GET_VM_CONSENTS, HttpStatus.NOT_FOUND, eventMap))
//            .onStatus(HttpStatus.BAD_REQUEST::equals, webClientHelper.onStatusCvsException(HttpStatus.BAD_REQUEST.name(), ErrorKey.GET_VM_CONSENTS, HttpStatus.BAD_REQUEST, eventMap))
//            .onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals, webClientHelper.onStatusCvsException(HttpStatus.UNPROCESSABLE_ENTITY.name(), ErrorKey.GET_VM_CONSENTS, HttpStatus.UNPROCESSABLE_ENTITY, eventMap))
//            .bodyToMono(VMGetConsentsResponse.class)
//            .doOnError(error -> logErrorEvent(eventMap, startTime))
//            .doOnSuccess(e -> logInfoEvent(eventMap, startTime));
//    }
//
//    @SuppressWarnings("unused")
//    public Mono<VMGetConsentsResponse> getVMConsentsFallback(
//        String id,
//        String idType,
//        GetConsentInput consentInput,
//        Map<String, Object> tags,
//        Map<String, String> headers,
//        Throwable error
//    ) {
//
//        return errorUtils.handleWebClientResponse(ErrorKey.GET_VM_CONSENTS, tags, error);
//    }
//
//    @Override
////    @CircuitBreaker(name = "setVMConsents", fallbackMethod = "setVMConsentsFallback")
////    @Retry(name = "setVMConsents")
//    public Mono<VMSetConsentsResponse> setVMConsents(
//        String id,
//        String idType,
//        SchedulingConsentInput schedulingConsentInput,
//        Map<String, Object> tags,
//        Map<String, String> headers
//    ) {
//
//        long startTime = System.currentTimeMillis();
//
//        String apiName = "setVMConsents";
//
//        Map<String, Object> eventMap = populateEventMap(
//            this.getClass().getName(),
//            apiName,
//            apiName,
//            apiName,
//            headers
//        );
//
//        VMSetConsentsRequest vmSetConsentsRequest = VMSetConsentsRequest.builder()
//            .id(id)
//            .idType(idType)
//            .appointmentId(schedulingConsentInput.getAppointmentId())
//            .operation(schedulingConsentInput.getOperationType())
//            .consentDataInput(schedulingConsentInput.getConsentDataInput())
//            .build();
//
//        return webClientHelper
//            .createWebClient(webClientVMSetConsents, vmSetConsentsRequest, VMSetConsentsRequest.class, ErrorKey.SET_VM_CONSENTS, headers, eventMap)
//            .onStatus(HttpStatus.NOT_FOUND::equals, webClientHelper.onStatusCvsException(HttpStatus.NOT_FOUND.name(), ErrorKey.SET_VM_CONSENTS, HttpStatus.NOT_FOUND, eventMap))
//            .onStatus(HttpStatus.BAD_REQUEST::equals, webClientHelper.onStatusCvsException(HttpStatus.BAD_REQUEST.name(), ErrorKey.SET_VM_CONSENTS, HttpStatus.BAD_REQUEST, eventMap))
//            .onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals, webClientHelper.onStatusCvsException(HttpStatus.UNPROCESSABLE_ENTITY.name(), ErrorKey.SET_VM_CONSENTS, HttpStatus.UNPROCESSABLE_ENTITY, eventMap))
//            .bodyToMono(VMSetConsentsResponse.class)
//            .doOnError(error -> logErrorEvent(eventMap, startTime))
//            .publishOn(Schedulers.boundedElastic())
//            .doOnSuccess(successResponse -> cacheUtils.setVMCache(id, idType, schedulingConsentInput.getConsentDataInput(), VM_CONSENT_DATA_KEY, headers, eventMap, startTime));
//    }
//
//    @SuppressWarnings("unused")
//    public Mono<VMSetConsentsResponse> setVMConsentsFallback(
//        String id,
//        String idType,
//        SchedulingConsentInput schedulingConsentInput,
//        Map<String, Object> tags,
//        Map<String, String> headerMap,
//        Throwable error
//    ) {
//
//        return errorUtils.handleWebClientResponse(ErrorKey.SET_VM_CONSENTS, tags, error);
//    }
//
////    @CircuitBreaker(name = "setVMQuestionnaireService", fallbackMethod = "fallVMQuestionnaireRequest")
////    @Retry(name = "setVMQuestionnaireService")
//    public Mono<VMSetQuestionnaireResponse> setVMQuestionnaire(
//        String id,
//        String idType,
//        SchedulingQuestionnaireInput schedulingQuestionnaireInput,
//        Map<String, Object> tags,
//        Map<String, String> headers
//    ) {
//
//        long startTime = System.currentTimeMillis();
//
//        String apiName = "setVMQuestionnaire";
//
//        Map<String, Object> eventMap = populateEventMap(
//            this.getClass().getName(),
//            apiName,
//            apiName,
//            apiName,
//            headers
//        );
//
//        VMSetQuestionnaireRequest vmSetQuestionnaireRequest = VMSetQuestionnaireRequest.builder()
//            .id(id)
//            .idType(idType)
//            .appointmentId(schedulingQuestionnaireInput.getAppointmentId())
//            .operation(schedulingQuestionnaireInput.getOperation())
//            .schedulingQuestionnaireDataInput(schedulingQuestionnaireInput.getSchedulingQuestionnaireDataInput())
//            .build();
//
//        return webClientHelper
//            .createWebClient(webClientVMSetQuestionnaire, vmSetQuestionnaireRequest, VMSetQuestionnaireRequest.class, ErrorKey.SET_VM_QUESTIONNAIRE, headers, eventMap)
//            .onStatus(HttpStatus.NOT_FOUND::equals, webClientHelper.onStatusCvsException(HttpStatus.NOT_FOUND.name(), ErrorKey.SET_VM_QUESTIONNAIRE, HttpStatus.NOT_FOUND, eventMap))
//            .onStatus(HttpStatus.BAD_REQUEST::equals, webClientHelper.onStatusCvsException(HttpStatus.BAD_REQUEST.name(), ErrorKey.SET_VM_QUESTIONNAIRE, HttpStatus.BAD_REQUEST, eventMap))
//            .onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals, webClientHelper.onStatusCvsException(HttpStatus.UNPROCESSABLE_ENTITY.name(), ErrorKey.SET_VM_QUESTIONNAIRE, HttpStatus.UNPROCESSABLE_ENTITY, eventMap))
//            .bodyToMono(VMSetQuestionnaireResponse.class)
//            .doOnError(error -> logErrorEvent(eventMap, startTime))
//            .publishOn(Schedulers.boundedElastic())
//            .doOnSuccess(successResponse -> cacheUtils.setVMCache(id, idType, schedulingQuestionnaireInput, VM_QUESTIONNAIRE_DATA_KEY, headers, eventMap, startTime));
//    }
//
//    @SuppressWarnings("unused")
//    public Mono<Object> fallVMQuestionnaireRequest(
//        String id,
//        String idType,
//        SchedulingQuestionnaireInput schedulingQuestionnaireInput,
//        Map<String, Object> tags,
//        Map<String, String> headerMap,
//        Throwable error
//    ) {
//
//        tags.put("setVMQuestionnaireFailed", Boolean.TRUE.toString());
//
//        return errorUtils.handleWebClientResponse(ErrorKey.SET_VM_QUESTIONNAIRE, tags, error);
//    }
//
//    @CircuitBreaker(name = "getVMAppointmentStatus", fallbackMethod = "vmAppointmentStatusFallback")
//    @Retry(name = "getVMAppointmentStatus")
//    public Mono<VMAppointmentStatusResponse> vmAppointmentStatus(
//        String id,
//        String idType,
//        String xid,
//        Map<String, Object> tags,
//        Map<String, String> headers
//    ) {
//
//        long startTime = System.currentTimeMillis();
//
//        String apiName = "vmAppointmentStatus";
//
//        Map<String, Object> eventMap = populateEventMap(
//                this.getClass().getName(),
//                apiName,
//                apiName,
//                apiName,
//                headers
//        );
//        VMAppointmentStatusRequest vmAppointmentStatusRequest = VMAppointmentStatusRequest.builder().xid(xid).id(id).idType(idType).build();
//
//        return webClientHelper
//            .createWebClient(webClientVMAppointmentStatus, vmAppointmentStatusRequest, VMAppointmentStatusRequest.class, ErrorKey.VM_APPOINTMENT_STATUS, headers, eventMap)
//            .onStatus(HttpStatus.NOT_FOUND::equals, webClientHelper.onStatusCvsException(APPOINTMENT_NOT_FOUND, ErrorKey.VM_APPOINTMENT_STATUS, HttpStatus.NOT_FOUND, eventMap))
//            .onStatus(HttpStatus.BAD_REQUEST::equals, webClientHelper.onStatusCvsException(HttpStatus.BAD_REQUEST.name(), ErrorKey.VM_APPOINTMENT_STATUS, HttpStatus.BAD_REQUEST, eventMap))
//            .onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals, webClientHelper.onStatusCvsException(HttpStatus.UNPROCESSABLE_ENTITY.name(), ErrorKey.VM_APPOINTMENT_STATUS, HttpStatus.UNPROCESSABLE_ENTITY, eventMap))
//            .bodyToMono(VMAppointmentStatusResponse.class)
//            .doOnError(error -> logErrorEvent(eventMap, startTime))
//            .doOnSuccess(response -> {
//                response.setStatusCode(SUCCESS_MSG);
//                response.setStatusDescription(SUCCESS_MSG);
//                logInfoEvent(eventMap, startTime);
//            });
//    }
//
//    @SuppressWarnings("unused")
//    public Mono<Object> vmAppointmentStatusFallback(
//        String id,
//        String idType,
//        String xid,
//        Map<String, Object> tags,
//        Map<String, String> headers,
//        Throwable error
//    ) {
//
//        tags.put("getVMAppointmentStatus", Boolean.TRUE.toString());
//
//        return errorUtils.handleWebClientResponse(ErrorKey.VM_APPOINTMENT_STATUS, tags, error);
//    }
//
////    @CircuitBreaker(name = "getVMAppointmentStatus", fallbackMethod = "getVMAppointmentStatusFallback")
////    @Retry(name = "getVMAppointmentStatus")
//    public Mono<VMAppointmentStatusResponse> getVMAppointmentStatus(String xid, Map<String, Object> tags, Map<String, String> headerMap) {
//        long startTime = System.currentTimeMillis();
//        Map<String, Object> eventMap = (tags.get("eventMap")!=null)?(new HashMap<>( (Map<String, Object>) tags.get("eventMap"))):new LinkedHashMap<>();
//        eventMap.put(SchedulingConstants.OPNAME, "getVMAppointmentStatus");
//        HttpHeaders httpHeaders =new HttpHeaders();
//        httpHeaders.add( HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE );
//        httpHeaders.add( HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE );
//        httpHeaders.add( SchedulingConstants.CONST_CATEGORY,headerMap.get(SchedulingConstants.CONST_CATEGORY));
//        httpHeaders.add( SchedulingConstants.CONST_X_GRID,headerMap.get(SchedulingConstants.CONST_X_GRID));
//        httpHeaders.add( SchedulingConstants.CONST_EXP_ID,headerMap.get(SchedulingConstants.CONST_EXP_ID));
//        httpHeaders.add( SchedulingConstants.CONST_CLIENTREFID,headerMap.get(SchedulingConstants.CONST_CLIENTREFID));
//
//        return webClientVMAppointmentStatus.get()
//                .uri(uriBuilder -> uriBuilder
//                        .queryParam("xid", xid)
//                        .build())
//                .headers(h -> h.addAll(httpHeaders))
//                .retrieve()
//                .bodyToMono(VMAppointmentStatusResponse.class)
//                .onErrorResume(throwable -> {
//                    tags.put("vmAppointmentStatusFailed", Boolean.TRUE.toString());
//                    eventMap.put("vmAppointmentStatusError", throwable.getMessage());
//                    eventMap.put(SchedulingConstants.STATUS_CDE, FAILURE_CD);
//                    eventMap.put(SchedulingConstants.STATUS_MESSAGE, FAILURE_MSG);
//                    long endTime = System.currentTimeMillis();
//                    long elapsedTime = endTime - startTime;
//                    eventMap.put(SchedulingConstants.RESP_TIME, elapsedTime);
//                    logErrorEvent(eventMap, startTime);
//                    return Mono.just(VMAppointmentStatusResponse.builder().build());
//                })
//                .doOnSuccess(vmAppointmentStatusResponse -> {
//                    if(StringUtils.isNotBlank(vmAppointmentStatusResponse.getAppointmentId())) {
//                        long endTime = System.currentTimeMillis();
//                        long elapsedTime = endTime - startTime;
//                        eventMap.put(SchedulingConstants.RESP_TIME, elapsedTime);
//                        eventMap.put(SchedulingConstants.STATUS_CDE, SUCCESS_MSG);
//                        eventMap.put(SchedulingConstants.STATUS_MESSAGE, SUCCESS_MSG);
//                        logInfoEvent(eventMap, startTime);
//                    }
//
//                });
//
//    }
//    @SuppressWarnings("unused")
//    public Mono<Object> getVMAppointmentStatusFallback(
//            String xid,
//            Map<String, Object> tags,
//            Map<String, String> headers,
//            Throwable error
//    ) {
//        tags.put("getVMAppointmentStatus", Boolean.TRUE.toString());
//        return errorUtils.handleWebClientResponse(ErrorKey.VM_APPOINTMENT_STATUS, tags, error);
//    }
//}