package com.example.eventanalytics.repo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.eventanalytics.domain.entity.EventEntity;

public interface EventRepository extends JpaRepository<EventEntity, UUID> {
  // Analytics queries come in later, so for now just extend JpaRepository.
}
