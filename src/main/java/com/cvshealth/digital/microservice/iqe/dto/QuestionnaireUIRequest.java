package com.cvshealth.digital.microservice.iqe.dto;

import com.cvshealth.digital.microservice.iqe.QuestionnaireContextEnum;
import lombok.Data;

import java.util.List;

@Data
public class QuestionnaireUIRequest {
    private QuestionnaireData data;
    @Data
    public static class QuestionnaireData {
        private String id;
        private String idType;
        private ScheduleQuestionnaireInput questionnaireInput;
    }

    @Data
    public static class ScheduleQuestionnaireInput {
        private String lob;
        private String storeId;
        private String clinicId;
        private String flow;
        private String modality;
        private String authType;
        private String source;
        private String state;
        private boolean sameDaySchedule;
        private String appointmentId;
        private List<QuestionnaireDataInput> questionnaireDataInput;
    }
    @Data
    public static class QuestionnaireDataInput {
        private String patientReferenceId;
        private String dateOfBirth;
        private Boolean isREAvailable;
        private Boolean isNOKAvailable;
        private List<QuestionnaireContextEnum> requiredQuestionnaireContext;
        private List<String> category;
        private List<VaccineInput> vaccines;
        private List<Services> services;
    }
    @Data
    public static class Services {
        private String name;
        private Integer reasonId;
        private String reasonMappingId;
    }
}