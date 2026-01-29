package com.example.eventanalytics.service;

import com.example.eventanalytics.domain.entity.EventEntity;
import com.example.eventanalytics.dto.EventDtos;
import com.example.eventanalytics.repo.EventRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class EventService {

  private final EventRepository events;

  public EventService(EventRepository events) {
    this.events = events;
  }

  public void create(EventDtos.CreateEventRequest req, Authentication auth) {
    UUID userId = extractUserId(auth);

    Instant occurredAt = (req.occurredAt() != null)
        ? req.occurredAt()
        : Instant.now();

    EventEntity event = new EventEntity(
        UUID.randomUUID(),
        userId,
        req.type(),
        req.entityType(),
        req.entityId(),
        occurredAt,
        req.metadata(),   // Map<String, Object> -> jsonb via JsonType
        Instant.now()
    );

    events.save(event);
  }

  private UUID extractUserId(Authentication auth) {
    Object principal = auth.getPrincipal();
    if (principal instanceof UUID uuid) return uuid;
    if (principal instanceof String s) return UUID.fromString(s);
    throw new IllegalStateException("Unsupported principal type: " + principal.getClass());
  }
}
