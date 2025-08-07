package com.cvshealth.digital.microservice.iqe.dto;

import lombok.Data;

import java.util.List;

@Data
public class Question {

    private String id;
    private String text;
    private String errorMessage;
    private String answerType;
    private boolean isStacked;
    private List<AnswerOptions> answerOptionsList;
    private String helpText;
    private Integer characterLimit;
}