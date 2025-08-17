package com.cvshealth.digital.microservice.iqe.dto;

import com.cvshealth.digital.microservice.iqe.enums.ConsentsEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SchedulingConsentInput {

    public String lob;
    public String authType;
    public String source;
    public String operationType;
    public String flow;
    public String appointmentId;
    public List<SchedulingConsentDataInput> consentDataInput;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SchedulingConsentDataInput{
        public String patientReferenceId;
        public String consentContext;
        public Boolean summarizedConsents;
        public List<ConsentsInput> consents;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ConsentsInput{
        public ConsentsEnum consentName;
        public Boolean consentValue;
        public String consentId;
        public String consentString;
        public String value;
        public String valueType;
        public Boolean isUpdateNeeded;
        public List<String> patientReferenceIds;
    }
}