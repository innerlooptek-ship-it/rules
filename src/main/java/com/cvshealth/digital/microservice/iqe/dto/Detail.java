package com.cvshealth.digital.microservice.iqe.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Detail {
    private String title;
    private String instructions;
    private String helper;
    @JsonProperty( value = "subcontext")
    private String subContext;
    private Integer pageNumber;
    private String footer;
}