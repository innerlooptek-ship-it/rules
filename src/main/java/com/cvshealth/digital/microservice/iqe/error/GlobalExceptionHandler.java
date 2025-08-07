package com.cvshealth.digital.microservice.iqe.error;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;


@Schema(description = "Exception handler for the application")
@RestControllerAdvice("com.cvshealth.digital")
@Order(-2)
public class GlobalExceptionHandler {

  @ExceptionHandler(ClientErrorException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorResponse> handleClientErrorException(ClientErrorException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                    new ErrorResponse(
                            HttpStatus.BAD_REQUEST.value(), ex.getErrorCode(), ex.getErrorDesc()));
  }

  @ExceptionHandler(ServerErrorException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<ErrorResponse> handleServerErrorException(ServerErrorException ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getErrorCode(), ex.getErrorDesc()));
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
      ResourceNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(
            new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getErrorCode(), ex.getErrorDesc()));
  }

  @ExceptionHandler(InvalidRequestException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseEntity<ErrorResponse> handleinvalidRequestException(
          InvalidRequestException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                    new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getErrorCode(), ex.getErrorDesc()));
  }

  @ExceptionHandler(RedisServerException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<ErrorResponse> handleRedisServerException(
          IQEBaseException ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                    new ErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getErrorCode(), ex.getErrorDesc()));
  }

}