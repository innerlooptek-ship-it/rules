package com.cvshealth.digital.microservice.iqe.controller;

import com.cvshealth.digital.microservice.iqe.QuestionnaireContextEnum;
import com.cvshealth.digital.microservice.iqe.config.MessageConfig;
import com.cvshealth.digital.microservice.iqe.dto.QuestionnaireUIRequest;
import com.cvshealth.digital.microservice.iqe.dto.QuestionnaireUIResponse;
import com.cvshealth.digital.microservice.iqe.dto.SchedulingMetricsService;
import com.cvshealth.digital.microservice.iqe.exception.CvsException;
import com.cvshealth.digital.microservice.iqe.service.SchedulingService;
import com.cvshealth.digital.microservice.iqe.udt.SchedulingConstants;
import com.cvshealth.digital.microservice.iqe.utils.LoggingUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.cvshealth.digital.microservice.iqe.udt.SchedulingConstants.*;
import static com.cvshealth.digital.microservice.iqe.udt.SchedulingConstants.TEST_TREAT;
import static com.cvshealth.digital.microservice.iqe.utils.LoggingUtils.populateEventMap;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Controller
@RequiredArgsConstructor
public class SchedulingController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final SchedulingService schedulingService;
    private final ObjectMapper objectMapper;
    private final LoggingUtils logUtils;
    private final MessageConfig messagesConfig;
    private final SchedulingMetricsService schedulingMetricsService;
    private final Map<Integer, List<String>> ELIGIBLE_REASON_IDS = Map.of(86,List.of(),30,List.of("48"));
    private String isVaccineIntakeEnabled;
    @QueryMapping
    public Mono<QuestionnaireUIResponse.GetQuestionnaire> getSchedulingQuestionnaire(
            @Argument String id,
            @Argument String idType,
            @Argument QuestionnaireUIRequest.ScheduleQuestionnaireInput questionnaireInput,
            @ContextValue("headers") Map<String, String> headerMap
    ) throws CvsException, IOException {
        long startTime = System.currentTimeMillis();

        Map<String, Object> eventMap = populateEventMap(
                "DHSSchedulingController",
                "getSchedulingQuestionnaire",
                "getSchedulingQuestionnaire",
                "getSchedulingQuestionnaire",
                headerMap
        );

        logUtils.entryEventLogging(logger, eventMap);

        Map<String, String> messageConfigs = messagesConfig.getMessages();
        if ((StringUtils.equalsIgnoreCase(questionnaireInput.getFlow(), TEST_TREAT)) && isNotEmpty(questionnaireInput.getQuestionnaireDataInput())) {
            return schedulingService.getIQEQuestionnaire(questionnaireInput, questionnaireInput.getQuestionnaireDataInput().get(0).getRequiredQuestionnaireContext().get(0), headerMap, eventMap)
                    .doOnSuccess(response -> {
                        long endTime = System.currentTimeMillis();
                        eventMap.put(RESP_TIME, endTime - startTime);
                        eventMap.put(STATUS_CDE, SUCCESS_CODE);
                        eventMap.put(STATUS_MESSAGE, SUCCESS_MSG);
                        logUtils.exitEventLogging(logger, eventMap);
                    }).doOnError(ex -> {
                        eventMap.put(STATUS_CDE, INTERNAL_SERVER_ERROR);
                        eventMap.put(STATUS_MESSAGE, ex.getMessage());
                        logUtils.errorEventLogging(logger, eventMap);
                    }).doFinally(res -> {
                        long endTime = System.currentTimeMillis();

                        schedulingMetricsService.incrementTransactionCounter("getSchedulingQuestionnaire", "getQuestionnarie");
                        schedulingMetricsService.recordResponseTime("getSchedulingQuestionnaire",
                                endTime - startTime, "getQuestionnarie");
                    });
        }
        if ((StringUtils.equalsIgnoreCase(questionnaireInput.getFlow(), MHC) || StringUtils.equalsIgnoreCase(questionnaireInput.getFlow(), EVC_B2B)) && isNotEmpty(questionnaireInput.getQuestionnaireDataInput()) &&
                isNotEmpty(questionnaireInput.getQuestionnaireDataInput().get(0).getRequiredQuestionnaireContext())) {
            if (Objects.equals(questionnaireInput.getQuestionnaireDataInput().get(0).getRequiredQuestionnaireContext().get(0), QuestionnaireContextEnum.MHC_SCHEDULING_QUESTION)) {
                return schedulingService.getIQEQuestionnaire(questionnaireInput, questionnaireInput.getQuestionnaireDataInput().get(0).getRequiredQuestionnaireContext().get(0), headerMap, eventMap)
                        .doOnSuccess(response -> {
                            long endTime = System.currentTimeMillis();
                            eventMap.put(RESP_TIME, endTime - startTime);
                            eventMap.put(STATUS_CDE, SUCCESS_CODE);
                            eventMap.put(STATUS_MESSAGE, SUCCESS_MSG);
                            logUtils.exitEventLogging(logger, eventMap);
                        }).doOnError(ex -> {
                            eventMap.put(STATUS_CDE, INTERNAL_SERVER_ERROR);
                            eventMap.put(STATUS_MESSAGE, ex.getMessage());
                            logUtils.errorEventLogging(logger, eventMap);
                        }).doFinally(res -> {
                            long endTime = System.currentTimeMillis();
                            schedulingMetricsService.incrementTransactionCounter("getSchedulingQuestionnaire", "getQuestionnarie");
                            schedulingMetricsService.recordResponseTime("getSchedulingQuestionnaire", endTime - startTime, "getQuestionnarie");
                        });
            }
        }
        if (
                (StringUtils.equalsIgnoreCase(questionnaireInput.getFlow(), MC_CORE) || StringUtils.equalsIgnoreCase(questionnaireInput.getFlow(), EVC_B2B))
                        && isNotEmpty(questionnaireInput.getQuestionnaireDataInput())
                        && isNotEmpty(questionnaireInput.getQuestionnaireDataInput().get(0).getServices())
                        && Objects.equals(questionnaireInput.getQuestionnaireDataInput().get(0).getRequiredQuestionnaireContext().get(0), QuestionnaireContextEnum.MC_CORE_ELIGIBILITY_QUESTION)
                        && isEligible(questionnaireInput)
        ) {
            return schedulingService.getMCCoreResponse(questionnaireInput, QuestionnaireContextEnum.MC_CORE_ELIGIBILITY_QUESTION, headerMap, eventMap)
                    .doOnSuccess(response -> {
                        long endTime = System.currentTimeMillis();
                        eventMap.put(RESP_TIME, endTime - startTime);
                        eventMap.put(STATUS_CDE, SUCCESS_CODE);
                        eventMap.put(STATUS_MESSAGE, SUCCESS_MSG);
                        logUtils.exitEventLogging(logger, eventMap);
                    }).doOnError(ex -> {
                        eventMap.put(STATUS_CDE, INTERNAL_SERVER_ERROR);
                        eventMap.put(STATUS_MESSAGE, ex.getMessage());
                        logUtils.errorEventLogging(logger, eventMap);
                    }).doFinally(res -> {
                        long endTime = System.currentTimeMillis();
                        schedulingMetricsService.incrementTransactionCounter("getSchedulingQuestionnaire", "getQuestionnarie");
                        schedulingMetricsService.recordResponseTime("getSchedulingQuestionnaire",
                                endTime - startTime, "getQuestionnarie");
                    });
        } else if ((StringUtils.equalsIgnoreCase(questionnaireInput.getFlow(), MC_CORE) || StringUtils.equalsIgnoreCase(questionnaireInput.getFlow(), EVC_B2B)) && isNotEmpty(questionnaireInput.getQuestionnaireDataInput())
                && isNotEmpty(questionnaireInput.getQuestionnaireDataInput().get(0).getServices())
                && Objects.equals(questionnaireInput.getQuestionnaireDataInput().get(0).getRequiredQuestionnaireContext().get(0), QuestionnaireContextEnum.MC_CORE_ELIGIBILITY_QUESTION)) {
            return schedulingService.getIQEQuestionnaire(questionnaireInput, questionnaireInput.getQuestionnaireDataInput().get(0).getRequiredQuestionnaireContext().get(0), headerMap, eventMap)
                    .doOnSuccess(response -> {
                        long endTime = System.currentTimeMillis();
                        eventMap.put(RESP_TIME, endTime - startTime);
                        eventMap.put(STATUS_CDE, SUCCESS_CODE);
                        eventMap.put(STATUS_MESSAGE, SUCCESS_MSG);
                        logUtils.exitEventLogging(logger, eventMap);
                    }).doOnError(ex -> {
                        eventMap.put(STATUS_CDE, INTERNAL_SERVER_ERROR);
                        eventMap.put(STATUS_MESSAGE, ex.getMessage());
                        logUtils.errorEventLogging(logger, eventMap);
                    }).doFinally(res -> {
                        long endTime = System.currentTimeMillis();
                        schedulingMetricsService.incrementTransactionCounter("getSchedulingQuestionnaire", "getQuestionnarie");
                        schedulingMetricsService.recordResponseTime("getSchedulingQuestionnaire", endTime - startTime, "getQuestionnarie");
                    });
        } else if ((StringUtils.equalsIgnoreCase(questionnaireInput.getFlow(), MC_CORE) || StringUtils.equalsIgnoreCase(questionnaireInput.getFlow(), MHC) || StringUtils.equalsIgnoreCase(questionnaireInput.getFlow(), EVC_B2B))
                && (Objects.equals(questionnaireInput.getQuestionnaireDataInput().get(0).getRequiredQuestionnaireContext().get(0), QuestionnaireContextEnum.MC_LEGAL_QUESTION)) || Objects.equals(questionnaireInput.getQuestionnaireDataInput().get(0).getRequiredQuestionnaireContext().get(0), QuestionnaireContextEnum.MHC_LEGAL_QUESTION)) {
            return schedulingService.getIQEQuestionnaire(questionnaireInput, questionnaireInput.getQuestionnaireDataInput().get(0).getRequiredQuestionnaireContext().get(0), headerMap, eventMap)
                    .doOnSuccess(response -> {
                        long endTime = System.currentTimeMillis();
                        eventMap.put(RESP_TIME, endTime - startTime);
                        eventMap.put(STATUS_CDE, SUCCESS_CODE);
                        eventMap.put(STATUS_MESSAGE, SUCCESS_MSG);
                        logUtils.exitEventLogging(logger, eventMap);
                    }).doOnError(ex -> {
                        eventMap.put(STATUS_CDE, INTERNAL_SERVER_ERROR);
                        eventMap.put(STATUS_MESSAGE, ex.getMessage());
                        logUtils.errorEventLogging(logger, eventMap);
                    }).doFinally(res -> {
                        long endTime = System.currentTimeMillis();
                        schedulingMetricsService.incrementTransactionCounter("getSchedulingQuestionnaire", "getQuestionnarie");
                        schedulingMetricsService.recordResponseTime("getSchedulingQuestionnaire", endTime - startTime, "getQuestionnarie");
                    });
        } else if ((StringUtils.equalsIgnoreCase(questionnaireInput.getFlow(), MC_CORE) || StringUtils.equalsIgnoreCase(questionnaireInput.getFlow(), MHC) || StringUtils.equalsIgnoreCase(questionnaireInput.getFlow(), EVC_B2B))
                && (Objects.equals(questionnaireInput.getQuestionnaireDataInput().get(0).getRequiredQuestionnaireContext().get(0), QuestionnaireContextEnum.MC_INSURANCE_COVERAGE))) {
            return schedulingService.getIQEQuestionnaire(questionnaireInput, questionnaireInput.getQuestionnaireDataInput().get(0).getRequiredQuestionnaireContext().get(0), headerMap, eventMap)
                    .doOnSuccess(response -> {
                        long endTime = System.currentTimeMillis();
                        eventMap.put(RESP_TIME, endTime - startTime);
                        eventMap.put(STATUS_CDE, SUCCESS_CODE);
                        eventMap.put(STATUS_MESSAGE, SUCCESS_MSG);
                        logUtils.exitEventLogging(logger, eventMap);
                    }).doOnError(ex -> {
                        eventMap.put(STATUS_CDE, INTERNAL_SERVER_ERROR);
                        eventMap.put(STATUS_MESSAGE, ex.getMessage());
                        logUtils.errorEventLogging(logger, eventMap);
                    }).doFinally(res -> {
                        long endTime = System.currentTimeMillis();
                        schedulingMetricsService.incrementTransactionCounter("getSchedulingQuestionnaire", "getQuestionnarie");
                        schedulingMetricsService.recordResponseTime("getSchedulingQuestionnaire", endTime - startTime, "getQuestionnarie");
                    });
        }

        if (questionnaireInput.getFlow() != null && SchedulingConstants.FLOW_VM.equalsIgnoreCase(questionnaireInput.getFlow())) {

            return schedulingService.getIQEQuestionnaire(questionnaireInput, questionnaireInput.getQuestionnaireDataInput().get(0).getRequiredQuestionnaireContext().get(0), headerMap, eventMap)
                    .doOnSuccess(response -> {
                        long endTime = System.currentTimeMillis();
                        eventMap.put(RESP_TIME, endTime - startTime);
                        eventMap.put(STATUS_CDE, SUCCESS_CODE);
                        eventMap.put(STATUS_MESSAGE, SUCCESS_MSG);
                        logUtils.exitEventLogging(logger, eventMap);
                    }).doOnError(ex -> {
                        eventMap.put(STATUS_CDE, INTERNAL_SERVER_ERROR);
                        eventMap.put(STATUS_MESSAGE, ex.getMessage());
                        logUtils.errorEventLogging(logger, eventMap);
                    }).doFinally(res -> {
                        long endTime = System.currentTimeMillis();
                        schedulingMetricsService.incrementTransactionCounter("getSchedulingQuestionnaire", "getQuestionnarie");
                        schedulingMetricsService.recordResponseTime("getSchedulingQuestionnaire", endTime - startTime, "getQuestionnarie");
                    });
        }


//        if (Objects.isNull(questionnaireInput.getLob())) {
//            return schedulingService.getImzQuestionnarie(questionnaireInput, headerMap, eventMap)
//                    .doOnSuccess(response -> {
//                        long endTime = System.currentTimeMillis();
//                        eventMap.put(RESP_TIME, endTime - startTime);
//                        eventMap.put(STATUS_CDE, SUCCESS_CODE);
//                        eventMap.put(STATUS_MESSAGE, SUCCESS_MSG);
//                        logUtils.exitEventLogging(logger, eventMap);
//                    }).doOnError(ex -> {
//                        eventMap.put(STATUS_CDE, INTERNAL_SERVER_ERROR);
//                        eventMap.put(STATUS_MESSAGE, ex.getMessage());
//                        logUtils.errorEventLogging(logger, eventMap);
//                    }).doFinally(res -> {
//                        long endTime = System.currentTimeMillis();
//                        schedulingMetricsService.incrementTransactionCounter("getSchedulingQuestionnaire", "getQuestionnarie");
//                        schedulingMetricsService.recordResponseTime("getSchedulingQuestionnaire",
//                                endTime - startTime, "getQuestionnarie");
//                    });
//        }
        if (StringUtils.equalsIgnoreCase(questionnaireInput.getLob(), CLINIC)
                && StringUtils.equalsIgnoreCase(questionnaireInput.getFlow(), VACCINE) && Objects.equals(isVaccineIntakeEnabled, "true")) {
            return schedulingService.getIQEQuestionnaire(
                            questionnaireInput,
                            questionnaireInput.getQuestionnaireDataInput().get(0).getRequiredQuestionnaireContext().get(0),
                            headerMap,
                            eventMap
                    )
                    .doOnSuccess(response -> {
                        long endTime = System.currentTimeMillis();
                        eventMap.put(RESP_TIME, endTime - startTime);
                        eventMap.put(STATUS_CDE, SUCCESS_CODE);
                        eventMap.put(STATUS_MESSAGE, SUCCESS_MSG);
                        logUtils.exitEventLogging(logger, eventMap);
                    }).doOnError(ex -> {
                        eventMap.put(STATUS_CDE, INTERNAL_SERVER_ERROR);
                        eventMap.put(STATUS_MESSAGE, ex.getMessage());
                        logUtils.errorEventLogging(logger, eventMap);
                    }).doFinally(res -> {
                        long endTime = System.currentTimeMillis();
                        schedulingMetricsService.incrementTransactionCounter("getSchedulingQuestionnaire", "mcVaccineQuestionnaire");
                        schedulingMetricsService.recordResponseTime("getSchedulingQuestionnaire",
                                endTime - startTime, "mcVaccineQuestionnaire");
                    });
        }
        if (questionnaireInput.getLob().equals(CLINIC)) {
            QuestionnaireUIResponse.GetQuestionnaire questionnaire = new QuestionnaireUIResponse.GetQuestionnaire();
            questionnaire.setQuestionnaireData(new ArrayList<>());
            questionnaire.setStatusDescription("No questions found");
            questionnaire.setStatusCode("SUCCESS");
            long endTime = System.currentTimeMillis();
            schedulingMetricsService.incrementTransactionCounter("getSchedulingQuestionnaire", "getQuestionnarie");
            schedulingMetricsService.recordResponseTime("getSchedulingQuestionnaire",
                    endTime - startTime, "getQuestionnarie");
            return Mono.just(questionnaire);
        }
//        } else if (questionnaireInput.getLob().equals(RX_IMZ)) {
//            String paddedNumber = String.format("%05d", Integer.parseInt(questionnaireInput.getStoreId()));
//            questionnaireInput.setStoreId(paddedNumber);
//            return schedulingService.getImzQuestionnarie(questionnaireInput, headerMap, eventMap)
//                    .doOnSuccess(response -> {
//                        long endTime = System.currentTimeMillis();
//                        eventMap.put(RESP_TIME, endTime - startTime);
//                        eventMap.put(STATUS_CDE, SUCCESS_CODE);
//                        eventMap.put(STATUS_MESSAGE, SUCCESS_MSG);
//                        logUtils.exitEventLogging(logger, eventMap);
//                    }).doOnError(ex -> {
//                        eventMap.put(STATUS_CDE, INTERNAL_SERVER_ERROR);
//                        eventMap.put(STATUS_MESSAGE, ex.getMessage());
//                        logUtils.errorEventLogging(logger, eventMap);
//                    }).doFinally(res -> {
//                        long endTime = System.currentTimeMillis();
//                        schedulingMetricsService.incrementTransactionCounter("getSchedulingQuestionnaire", "getQuestionnarie");
//                        schedulingMetricsService.recordResponseTime("getSchedulingQuestionnaire",
//                                endTime - startTime, "getQuestionnarie");
//                    });
//        }
       else {
            CvsException ex = new CvsException(HttpStatus.BAD_REQUEST.value(),
                    "INVALID_LOB", messageConfigs.get("getQuestionnarie.INVALID_LOB"), messageConfigs.get("getQuestionnarie.INVALID_LOB"), ERROR_BAD_REQUEST);
            eventMap.put(STATUS_CDE, INTERNAL_SERVER_ERROR);
            eventMap.put(STATUS_MESSAGE, ex.getMessage());
            logUtils.errorEventLogging(logger, eventMap);
            long endTime = System.currentTimeMillis();
            schedulingMetricsService.incrementTransactionCounter("getSchedulingQuestionnaire", "getQuestionnarie");
            schedulingMetricsService.recordResponseTime("getSchedulingQuestionnaire",
                    endTime - startTime, "getQuestionnarie");
            throw ex;
        }
    }
    private boolean isEligible(QuestionnaireUIRequest.ScheduleQuestionnaireInput questionnaireInput){
        List<String> reasonMappingIds = ELIGIBLE_REASON_IDS.get(questionnaireInput.getQuestionnaireDataInput().get(0).getServices().get(0).getReasonId());
        if(reasonMappingIds != null){
            if(reasonMappingIds.isEmpty()){
                return true;
            }
            return questionnaireInput.getQuestionnaireDataInput().get(0).getServices().get(0).getReasonMappingId() != null && reasonMappingIds.contains(questionnaireInput.getQuestionnaireDataInput().get(0).getServices().get(0).getReasonMappingId());
        }
        return false;
    }
}
