package com.cvshealth.digital.microservice.iqe.dto;

import lombok.Data;
import java.util.List;

@Data
public class ImzQuestionnarieResponse {
    private ImmunizationQuestionsData data;


    @Data
    public static class VaccineRef {
        private String code;
        private String refId;
    }


    @Data
    public static class Questions {
        private String id;
        private String text;
        private String errorMessage;
        private String answerType;
        private List<VaccineRef> vaccineRef;
        private List<AnswerOption> answerOptions;

    }
    @Data
    public  static class AnswerOption {
        private String text;
        private String value;
        private Boolean additionalDetail;
        private String additionalDetailType;
        private String additionalDetailText;
        private List<RelatedQuestions> relatedQuestion;
        private List<RelatedQuestions> relatedQuestions;
    }

    @Data
    public static class RelatedQuestions {
        private String answerType;
        private String text;
        private Boolean required;
        private String errorMessage;
        private String id;
        private String questionId;
        private String helpText;
        private String characterLimit;
        private boolean stacked;
        private List<AnswerOption> answerOptions;
    }

    @Data
    public static class GetImmunizationQuestions {
        private String context;
        private List<Questions> questions;
    }

    @Data
    public static class ImmunizationQuestionsData {
        private List<GetImmunizationQuestions> getImmunizationQuestions;
    }
}