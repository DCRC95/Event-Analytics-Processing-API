/*
- @NotBlank → empty strings rejected
- Max lengths match DB schema
- occurredAt optional (defaults to now()).
*/

package com.example.eventanalytics.dto;

import java.time.Instant;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EventDtos {

  public record CreateEventRequest(
      @NotBlank @Size(max = 64) String type,
      @NotBlank @Size(max = 64) String entityType,
      @NotBlank @Size(max = 128) String entityId,
      Instant occurredAt,
      Map<String, Object> metadata
  ) {}
}
