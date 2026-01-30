package com.example.eventanalytics.exception;

import java.time.Instant;
import java.util.Map;

public class ErrorDtos {

  public record ApiError(
      Instant timestamp,
      int status,
      String error,
      String message,
      String path,
      Map<String, String> validationErrors
  ) {}
}
