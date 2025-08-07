package com.cvshealth.digital.microservice.iqe.error;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class IQEException extends IQEBaseException {
  public String additionalInfo;
  public String errorDetails;

  public IQEException(
      String errorCode, String errorDesc, String additionalInfo, String errorDetails) {
    super(errorCode, errorDesc, LocalDateTime.now());
    this.additionalInfo = additionalInfo;
    this.errorDetails = errorDetails;
  }
}