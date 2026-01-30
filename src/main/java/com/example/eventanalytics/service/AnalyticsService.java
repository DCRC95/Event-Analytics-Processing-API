package com.example.eventanalytics.service;

import com.example.eventanalytics.dto.AnalyticsDtos;
import com.example.eventanalytics.repo.EventRepository;
import com.example.eventanalytics.repo.projection.TypeCountProjection;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AnalyticsService {

  private final EventRepository events;

  public AnalyticsService(EventRepository events) {
    this.events = events;
  }

  public AnalyticsDtos.SummaryResponse getSummary(UUID userId, LocalDate from, LocalDate to) {
    Range range = toUtcRange(from, to);

    long total = events.countInRange(userId, range.startInclusive(), range.endExclusive());

    Map<String, Long> countsByType = toCountsMap(
        events.countByTypeInRange(userId, range.startInclusive(), range.endExclusive())
    );

    List<AnalyticsDtos.DailyTotal> dailyTotals = events.dailyTotalsInRange(userId, range.startInclusive(), range.endExclusive())
        .stream()
        .map(v -> new AnalyticsDtos.DailyTotal(v.getDate(), v.getCount()))
        .toList();

    List<AnalyticsDtos.TopEntity> topEntities = events.topEntitiesInRange(userId, range.startInclusive(), range.endExclusive(), 5)
        .stream()
        .map(v -> new AnalyticsDtos.TopEntity(v.getEntityType(), v.getEntityId(), v.getCount()))
        .toList();

    return new AnalyticsDtos.SummaryResponse(from, to, total, countsByType, dailyTotals, topEntities);
  }

  public List<AnalyticsDtos.TopEntity> getTop(UUID userId, String type, LocalDate from, LocalDate to, int limit) {
    Range range = toUtcRange(from, to);

    int safeLimit = Math.max(1, Math.min(limit, 100));

    return events.topEntitiesByTypeInRange(userId, type, range.startInclusive(), range.endExclusive(), safeLimit)
        .stream()
        .map(v -> new AnalyticsDtos.TopEntity(v.getEntityType(), v.getEntityId(), v.getCount()))
        .toList();
  }

  private Map<String, Long> toCountsMap(List<TypeCountProjection> rows) {
    // LinkedHashMap preserves query order (useful if repo orders by count desc)
    Map<String, Long> out = new LinkedHashMap<>();
    for (TypeCountProjection r : rows) out.put(r.getType(), r.getCount());
    return out;
  }

  /**
   * Range semantics:
   * startInclusive = from at 00:00 UTC
   * endExclusive   = (to + 1 day) at 00:00 UTC
   */
  private Range toUtcRange(LocalDate from, LocalDate to) {
    if (from == null || to == null) {
      throw new IllegalArgumentException("from and to are required");
    }
    if (to.isBefore(from)) {
      throw new IllegalArgumentException("to must be >= from");
    }

    Instant start = from.atStartOfDay(ZoneOffset.UTC).toInstant();
    Instant end = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    return new Range(start, end);
  }

  private record Range(Instant startInclusive, Instant endExclusive) {}
}
