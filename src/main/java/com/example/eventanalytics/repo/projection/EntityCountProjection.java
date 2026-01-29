package com.example.eventanalytics.repo.projection;

public interface EntityCountProjection {
  String getEntityType();
  String getEntityId();
  long getCount();
}

