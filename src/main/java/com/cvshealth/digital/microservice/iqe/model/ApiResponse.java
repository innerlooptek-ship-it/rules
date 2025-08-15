package com.cvshealth.digital.microservice.iqe.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder=true)
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse {
    private String statusCode;
    private String statusDescription;
}