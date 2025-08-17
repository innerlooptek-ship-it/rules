package com.cvshealth.digital.microservice.iqe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MCITGetPatientConsentsRequest {
    @JsonProperty("Request")
    private Request request;

    @Data
    @Builder
    public static class Request {
        @JsonProperty("GetPatientConsent2021Request")
        private GetPatientConsent2021Request getPatientConsent2021Request;

        @JsonProperty("Header")
        private Header header;
    }

    @Data
    @Builder
    public static class GetPatientConsent2021Request {
        @JsonProperty("ClinicId")
        private Integer clinicId;

        @JsonProperty("LOBType")
        private int lobType;

        @JsonProperty("ConsentList")
        private List<Consent> consentList;

        @JsonProperty("DOB")
        private String dob;

        @JsonProperty("Language")
        private String language;

        @JsonProperty("PatientId")
        private String patientId;

        @JsonProperty("State")
        private String state;
    }

    @Data
    @Builder
    public static class Header {
        @JsonProperty("ApplicationName")
        private String applicationName;

        @JsonProperty("CorrelationID")
        private String correlationId;

        @JsonProperty("UserID")
        private String userId;
    }

    @Data
    @Builder
    public static class Consent {
        @JsonProperty("ConsentKey")
        private String consentKey;
    }
}