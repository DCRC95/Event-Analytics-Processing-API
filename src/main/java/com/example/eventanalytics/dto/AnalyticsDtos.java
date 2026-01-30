package com.example.eventanalytics.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class AnalyticsDtos {

  public record DailyTotal(LocalDate date, long count) {}

  public record TopEntity(String entityType, String entityId, long count) {}

  public record SummaryResponse(
      LocalDate from,
      LocalDate to,
      long totalEvents,
      Map<String, Long> countsByType,
      List<DailyTotal> dailyTotals,
      List<TopEntity> topEntities
  ) {}
}
