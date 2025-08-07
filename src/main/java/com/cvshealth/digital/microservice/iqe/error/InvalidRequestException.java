package com.cvshealth.digital.microservice.iqe.error;

import java.time.LocalDateTime;

public class InvalidRequestException extends IQEBaseException {

    public InvalidRequestException(String errorCode, String errorDesc) {
        super(errorCode, errorDesc, LocalDateTime.now());
    }
}