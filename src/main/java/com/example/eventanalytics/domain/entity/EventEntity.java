package com.example.eventanalytics.domain.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "events")
public class EventEntity {

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

  @Column(columnDefinition = "jsonb")
  private String metadata;

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
      String metadata,
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

  public UUID getId() { return id; }
  public UUID getUserId() { return userId; }
  public String getType() { return type; }
  public String getEntityType() { return entityType; }
  public String getEntityId() { return entityId; }
  public Instant getOccurredAt() { return occurredAt; }
  public String getMetadata() { return metadata; }
  public Instant getCreatedAt() { return createdAt; }
}
