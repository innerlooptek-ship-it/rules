package com.cvshealth.digital.microservice.iqe.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetConsentInput {

    private String flow;
    private String appointmentId;
    private List<ConsentDataInput> consentsDataInput;
    private String lob;
    private String modality;
    private String brand;
    private String clinicId;
    private String state;
    private String authType;
    private List<ConsentContextInput> consentContextInput;


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ConsentDataInput {
        private String patientReferenceId;
        private List<String> consentContext;
        private String dateOfBirth;
        private String relation;
        private String encMCPatientId;

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ConsentContextInput {
        private String consentContext;
        private boolean summarize;

    }

}