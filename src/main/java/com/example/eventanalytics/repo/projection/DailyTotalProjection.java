package com.example.eventanalytics.repo.projection;

import java.time.LocalDate;

public interface DailyTotalProjection {
  LocalDate getDate();
  long getCount();
}

