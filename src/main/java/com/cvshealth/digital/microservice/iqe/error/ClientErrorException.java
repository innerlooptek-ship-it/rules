package com.cvshealth.digital.microservice.iqe.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;


public class ClientErrorException extends IQEBaseException {

    public ClientErrorException(String errorCode, String errorDesc) {
        super(errorCode, errorDesc, java.time.LocalDateTime.now());
    }
}
