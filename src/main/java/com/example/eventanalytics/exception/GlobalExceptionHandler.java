package com.example.eventanalytics.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
    return ResponseEntity
        .status(ex.getStatus())
        .body(new ErrorResponse(ex.getMessage()));
  }

  public record ErrorResponse(String message) {}
}

