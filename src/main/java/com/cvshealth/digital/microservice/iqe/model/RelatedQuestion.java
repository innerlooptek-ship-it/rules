package com.cvshealth.digital.microservice.iqe.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RelatedQuestion {
    private String id;
    private String text;
    private String errorMessage;
    private String answerType;
    private Boolean required;
    private List<AnswerOption> answerOptions;
    private String characterLimit;
    private String helpText;

}