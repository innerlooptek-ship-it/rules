package com.cvshealth.digital.microservice.iqe.dto;

import lombok.Data;

import java.util.List;

@Data
public class VaccineInput {
    private String code;

    private String type;

    private List<NdcInput> ndc;


}