package com.cvshealth.digital.microservice.iqe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Generated;

import java.util.List;

@Data
@Builder
public class MCITGetPatientConsentsResponse {
    @JsonProperty("Response")
    private Response response;

    @Data
    public static class Response {
        @JsonProperty("GetPatientConsent2021Response")
        private GetPatientConsent2021Response getPatientConsent2021Response;

        @JsonProperty("StatusRec")
        private StatusRec statusRec;
    }

    @Data
    public static class GetPatientConsent2021Response {
        @JsonProperty("ConsentAcknowledgementList")
        private List<ConsentAcknowledgement> consentAcknowledgementList;
    }

    @Data
    public static class ConsentAcknowledgement {
        @JsonProperty("ConsentGiveDate")
        private String consentGiveDate;

        @JsonProperty("ConsentKey")
        private String consentKey;

        @JsonProperty("FileName")
        private String fileName;

        @JsonProperty("IsAcknowledged")
        private boolean isAcknowledged;

        @JsonProperty("Language")
        private String language;

        @JsonProperty("MasterConsentID")
        private int masterConsentId;

        @JsonProperty("Value")
        private String value;
    }

    @Data
    public static class StatusRec {
        @JsonProperty("ReasonCode")
        private String reasonCode;

        @JsonProperty("ReferenceCode")
        private String referenceCode;

        @JsonProperty("Severity")
        private String severity;

        @JsonProperty("StatusCode")
        private int statusCode;

        @JsonProperty("StatusDesc")
        private String statusDesc;
    }
}