package com.example.eventanalytics.domain.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;

import java.util.Map;


@Entity
@Table(name = "events")
public class EventEntity {

  @Type(JsonType.class)
  @Column(columnDefinition = "jsonb")
  private Map<String, Object> metadata;
  
  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(nullable = false, length = 64)
  private String type;

  @Column(name = "entity_type", nullable = false, length = 64)
  private String entityType;

  @Column(name = "entity_id", nullable = false, length = 128)
  private String entityId;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected EventEntity() {}

public EventEntity(
    UUID id,
    UUID userId,
    String type,
    String entityType,
    String entityId,
    Instant occurredAt,
    Map<String, Object> metadata,
    Instant createdAt
) {
  this.id = id;
  this.userId = userId;
  this.type = type;
  this.entityType = entityType;
  this.entityId = entityId;
  this.occurredAt = occurredAt;
  this.metadata = metadata;
  this.createdAt = createdAt;
}
public Map<String, Object> getMetadata() { return metadata; }

}