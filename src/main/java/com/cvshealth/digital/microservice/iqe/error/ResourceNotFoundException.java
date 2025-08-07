package com.cvshealth.digital.microservice.iqe.error;

import java.time.LocalDateTime;

public class ResourceNotFoundException extends IQEBaseException {

  public ResourceNotFoundException(String errorCode, String errorDesc) {
    super(errorCode, errorDesc, LocalDateTime.now());
  }
}