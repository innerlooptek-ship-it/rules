package com.cvshealth.digital.microservice.iqe.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ErrorResponse {
    public int statusCode;
    public String code;
    public String description;
}
