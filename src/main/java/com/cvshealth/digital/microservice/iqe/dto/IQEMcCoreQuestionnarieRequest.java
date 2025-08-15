package com.cvshealth.digital.microservice.iqe.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IQEMcCoreQuestionnarieRequest {
    private String requiredQuestionnaireContext;
    private String flow;
    private Integer reasonId;
    private Integer reasonMappingId;
    private String state;
    private Integer age;
    private String modality;
}