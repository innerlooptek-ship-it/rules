package com.cvshealth.digital.microservice.iqe.error;

import java.time.LocalDateTime;

public class RedisServerException extends IQEBaseException {

    public RedisServerException(String errorCode, String errorDesc) {
        super(errorCode, errorDesc, LocalDateTime.now());
    }
}