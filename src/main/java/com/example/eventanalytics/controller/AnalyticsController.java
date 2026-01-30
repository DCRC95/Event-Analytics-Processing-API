package com.example.eventanalytics.controller;

import com.example.eventanalytics.dto.AnalyticsDtos;
import com.example.eventanalytics.service.AnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

  private final AnalyticsService analytics;

  public AnalyticsController(AnalyticsService analytics) {
    this.analytics = analytics;
  }

  @GetMapping("/summary")
  public AnalyticsDtos.SummaryResponse summary(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      Authentication auth
  ) {
    UUID userId = extractUserId(auth);
    return analytics.getSummary(userId, from, to);
  }

  @GetMapping("/top")
  public List<AnalyticsDtos.TopEntity> top(
      @RequestParam String type,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      @RequestParam(defaultValue = "5") int limit,
      Authentication auth
  ) {
    UUID userId = extractUserId(auth);
    return analytics.getTop(userId, type, from, to, limit);
  }

  private UUID extractUserId(Authentication auth) {
    Object principal = auth.getPrincipal();
    if (principal instanceof UUID uuid) return uuid;
    if (principal instanceof String s) return UUID.fromString(s);
    throw new IllegalStateException("Unsupported principal type: " + principal.getClass());
  }
}
