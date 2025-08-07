package com.cvshealth.digital.microservice.iqe.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Valid
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RulesDetails {

    @NotBlank(message = "flow must not be blank")
    private String flow;
    private Double age;
    private String code;
    private String questionId;
    private String answerValue;
    private  String serviceDescription;
    private String schedulingCategory;
    private String questions;
    private String action;
    private String requiredQuestionnaireContext;
    private  Integer reasonId;
    private Integer reasonMappingId;
    private String state;
    private String lob;
    private String modality;
    String actionId;
}