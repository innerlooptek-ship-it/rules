package com.cvshealth.digital.microservice.iqe.dto;


import lombok.Data;

import java.util.List;

@Data
public class QuestionnaireUIResponse {
    private String statusCode;
    private String statusDescription;
    private QuestionnaireDataOutput questionnaireData;
    @Data
    public static class QuestionnaireDataOutput {
        private GetQuestionnaire getQuestionnaire;
    }
    @Data
    public static class GetQuestionnaire {
        private String statusDescription;
        private String flow;
        private List<QuestionnaireData> questionnaireData;
        private String statusCode;
    }
    @Data
    public static class QuestionnaireData {
        private String patientReferenceId;
        private String context;
        private String appointmentId;
        private List<Detail> details;
        private List<Question> questions;
    }
    @Data
    public static class Question {
        private String id;
        private String questionId;
        private String text;
        private String category;
        private String errorMessage;
        private String helpText;
        private String answerType;
        private String linkText;
        private boolean required;
        private Boolean isStacked;
        private boolean stacked;
        private String characterLimit;
        private List<ImzQuestionnarieResponse.AnswerOption> answerOptions;
        private List<Services> services;
        private List<OVaccine> vaccines;
        private List<String> riskFactorFlag;
        private String subContext;
        private Integer questionNumber;
        private Integer sequenceId;
        private String skipLegend;
    }
    @Data
    public static class Services {
        private String name;
        private Integer reasonId;
        private String reasonMappingId;
    }
}