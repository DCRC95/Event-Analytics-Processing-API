package com.example.eventanalytics.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.eventanalytics.dto.EventDtos;
import com.example.eventanalytics.service.EventService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/events")
public class EventController {

  private final EventService events;

  public EventController(EventService events) {
    this.events = events;
  }

  @PostMapping
  public void create(
      @Valid @RequestBody EventDtos.CreateEventRequest req,
      Authentication auth
  ) {
    events.create(req, auth);
  }
}
