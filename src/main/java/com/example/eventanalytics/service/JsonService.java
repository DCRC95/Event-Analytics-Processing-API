package com.example.eventanalytics.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class JsonService {

  private final ObjectMapper mapper = new ObjectMapper();

  public String toJson(Object value) {
    if (value == null) return null;
    try {
      return mapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Invalid metadata JSON");
    }
  }
}
