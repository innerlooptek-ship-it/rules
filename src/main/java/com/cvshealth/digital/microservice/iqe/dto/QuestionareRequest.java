package com.cvshealth.digital.microservice.iqe.dto;

import com.cvshealth.digital.microservice.iqe.entity.RulesByFlowEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionareRequest {
    @NotNull(message = "RulesByFlow is null")
    @Valid
    private RulesByFlow rulesByFlow;

    @NotNull(message = "Actions is null")
    private Actions actions;

    @NotNull(message = "questions is null")
    @NotEmpty(message = "questions is empty")
    @Valid
    private List<Questions> questions;
    private List<Details> details;
    private List<RulesByFlowEntity> activeRules;

    private List<RulesByFlowEntity> inactiveRules;


    private String errorDescription;

    private String statusCode;

    private Questions question;
}