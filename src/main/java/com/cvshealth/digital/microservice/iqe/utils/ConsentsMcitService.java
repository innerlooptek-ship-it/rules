package com.cvshealth.digital.microservice.iqe.utils;

import com.cvshealth.digital.microservice.iqe.config.WebClientHelper;
import com.cvshealth.digital.microservice.iqe.constants.DhsCoreConstants;
import com.cvshealth.digital.microservice.iqe.constants.SchedulingConstants;
import com.cvshealth.digital.microservice.iqe.dto.LobTypeIdEnum;
import com.cvshealth.digital.microservice.iqe.dto.MCITGetPatientConsentsRequest;
import com.cvshealth.digital.microservice.iqe.dto.MCITGetPatientConsentsResponse;
import com.cvshealth.digital.microservice.iqe.enums.ErrorKey;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.cvshealth.digital.microservice.iqe.constants.DhsCoreConstants.CONST_X_GRID;
import static com.cvshealth.digital.microservice.iqe.utils.CvsLoggingUtil.logErrorEvent;
import static com.cvshealth.digital.microservice.iqe.utils.LoggingUtils.populateEventMap;


@Service
public class ConsentsMcitService {

    private final WebClient mcitGetPatientConsentsWebClient;

    private final WebClientHelper webClientHelper;

    @Autowired
    public ConsentsMcitService(
            WebClientHelper webClientHelper,
            @Qualifier("mcitGetPatientConsents") WebClient mcitGetPatientConsentsWebClient
    ) {
        this.webClientHelper = webClientHelper;
        this.mcitGetPatientConsentsWebClient = mcitGetPatientConsentsWebClient;
    }

    public Mono<MCITGetPatientConsentsResponse> getMCITPatientConsents(
            String patientId,
            String dob,
            String lob,
            String state,
            String clinicId,
            Map<String, Object> tags,
            Map<String,String> headers
    ) {

        long startTime = System.currentTimeMillis();

        String apiName = "getMCITPatientConsents";

        Map<String, Object> eventMap = populateEventMap(
                this.getClass().getName(),
                apiName,
                apiName,
                apiName,
                headers
        );
        List<MCITGetPatientConsentsRequest.Consent> consents = List.of(
                MCITGetPatientConsentsRequest.Consent.builder().consentKey("nop").build(),
                MCITGetPatientConsentsRequest.Consent.builder().consentKey("srvm").build(),
                MCITGetPatientConsentsRequest.Consent.builder().consentKey("TCPA").build(),
                MCITGetPatientConsentsRequest.Consent.builder().consentKey("njiis").build()
        );

        MCITGetPatientConsentsRequest.GetPatientConsent2021Request request = MCITGetPatientConsentsRequest.GetPatientConsent2021Request.builder()
                .patientId(patientId)
                .clinicId(StringUtils.isNotBlank(clinicId) ? Integer.parseInt(clinicId): null)
                .dob(StringUtils.isNotBlank(dob) ? CommonDateUtil.formatDateToFormat(dob, DhsCoreConstants.YYYY_MM_DD,DhsCoreConstants.MM_DD_YYYY): null)
                .state(state)
                .lobType(LobTypeIdEnum.valueOf(lob).getId())
                .consentList(consents).language("EN").build();
        MCITGetPatientConsentsRequest.Header header = MCITGetPatientConsentsRequest.Header.builder()
                .applicationName("MCDigital")
                .userId("Digital")
                .correlationId(headers.get(CONST_X_GRID) != null ? headers.get(CONST_X_GRID) : UUID.randomUUID().toString())
                .build();

        MCITGetPatientConsentsRequest mcitGetPatientConsentsRequest = MCITGetPatientConsentsRequest.builder()
                .request(MCITGetPatientConsentsRequest.Request.builder().getPatientConsent2021Request(request).header(header).build()).build();

        return webClientHelper
                .createWebClient(mcitGetPatientConsentsWebClient, mcitGetPatientConsentsRequest, MCITGetPatientConsentsRequest.class, ErrorKey.MCIT_GET_PATIENT_CONSENTS, headers, eventMap)
                .bodyToMono(MCITGetPatientConsentsResponse.class)
                .doOnSuccess(e -> {

                    long endTime = System.currentTimeMillis();
                    long elapsedTime = endTime - startTime;
                    tags.put(SchedulingConstants.MC_GET_APPOINTMENTS_RESP_TIME, elapsedTime);
                    tags.put("mcitGetPatientConsents_" + SchedulingConstants.RESP_TIME, elapsedTime);
                    if(e.getResponse() != null && e.getResponse().getStatusRec() != null && e.getResponse().getStatusRec().getStatusCode() != 0) {
                        eventMap.put(SchedulingConstants.STATUS_CDE, String.valueOf(e.getResponse().getStatusRec().getStatusCode()));
                        eventMap.put(SchedulingConstants.STATUS_MESSAGE, e.getResponse().getStatusRec().getStatusDesc());
                        tags.put("mcitGetPatientConsentsFailed", Boolean.TRUE.toString());
                        logErrorEvent(eventMap, startTime);
                    } else {
                        tags.put("mcitGetPatientConsents_" + startTime + "_" + SchedulingConstants.STATUS_CDE, SchedulingConstants.SUCCESS_CODE);
                        tags.put("mcitGetPatientConsents_" + startTime + "_" + SchedulingConstants.STATUS_MESSAGE, SchedulingConstants.SUCCESS_MSG);
                    }

                })
                .onErrorResume(throwable -> {
                    long endTime = System.currentTimeMillis();
                    long elapsedTime = endTime - startTime;
                    tags.put("mcitGetPatientConsentsFailed", Boolean.TRUE.toString());
                    eventMap.put(SchedulingConstants.MC_GET_APPOINTMENTS_RESP_TIME, elapsedTime);
                    eventMap.put(SchedulingConstants.RESP_TIME, elapsedTime);
                    eventMap.put(SchedulingConstants.STATUS_MESSAGE, throwable.getMessage());
                    eventMap.put(SchedulingConstants.STATUS_CDE, SchedulingConstants.FAILURE_CD);

                    logErrorEvent(eventMap, startTime);

                    return Mono.just(MCITGetPatientConsentsResponse.builder().build());
                });

    }
}