package com.cvshealth.digital.microservice.iqe.error;

import java.time.LocalDateTime;

public class ServerErrorException extends IQEBaseException {

  public ServerErrorException(String errorCode, String errorDesc) {
    super(errorCode, errorDesc, LocalDateTime.now());
  }
}