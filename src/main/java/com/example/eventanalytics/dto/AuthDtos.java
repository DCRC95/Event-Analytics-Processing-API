package com.example.eventanalytics.dto;

import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {

  public record RegisterRequest(
      @Email @NotBlank String email,
      @NotBlank @Size(min = 8, max = 72) String password
  ) {}

  public record LoginRequest(
      @Email @NotBlank String email,
      @NotBlank String password
  ) {}

  public record AuthResponse(
      UUID userId,
      String tokenType,
      String accessToken
  ) {}
}
