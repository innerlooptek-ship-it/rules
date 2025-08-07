package com.cvshealth.digital.microservice.iqe.error;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class IQEBaseException extends RuntimeException {
  private final String errorCode;
  private final String errorDesc;
  private final LocalDateTime timestamp;

  public IQEBaseException(
      String errorCode, String errorDesc, LocalDateTime timestamp) {
    super(errorDesc);
    this.errorCode = errorCode;
    this.errorDesc = errorDesc;
    this.timestamp = timestamp;
  }
}