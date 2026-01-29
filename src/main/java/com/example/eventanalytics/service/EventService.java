package com.example.eventanalytics.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.eventanalytics.domain.entity.EventEntity;
import com.example.eventanalytics.dto.EventDtos;
import com.example.eventanalytics.repo.EventRepository;


@Service
public class EventService {

  private final EventRepository events;
  private final JsonService jsonService;

  public EventService(EventRepository events, JsonService jsonService) {
    this.events = events;
    this.jsonService = jsonService;
  }

  public void create(EventDtos.CreateEventRequest req, Authentication auth) {
    UUID userId = (UUID) auth.getPrincipal();

    Instant occurredAt = req.occurredAt() != null
        ? req.occurredAt()
        : Instant.now();

    EventEntity event = new EventEntity(
        UUID.randomUUID(),
        userId,
        req.type(),
        req.entityType(),
        req.entityId(),
        occurredAt,
        jsonService.toJson(req.metadata()),
        Instant.now()
    );

    events.save(event);
  }
}
