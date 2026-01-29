package com.example.eventanalytics.service;

import com.example.eventanalytics.domain.entity.UserEntity;
import com.example.eventanalytics.dto.AuthDtos;
import com.example.eventanalytics.repo.UserRepository;
import com.example.eventanalytics.exception.ApiException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class AuthService {

  private final UserRepository users;
  private final PasswordEncoder encoder;
  private final JwtService jwt;

  public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
    this.users = users;
    this.encoder = encoder;
    this.jwt = jwt;
  }

  public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest req) {
    if (users.existsByEmailIgnoreCase(req.email())) {
      throw new ApiException(CONFLICT, "Email already registered");
    }

    UserEntity user = new UserEntity(
        UUID.randomUUID(),
        req.email().toLowerCase(),
        encoder.encode(req.password()),
        Instant.now()
    );
    users.save(user);

    String token = jwt.issue(user.getId(), user.getEmail());
    return new AuthDtos.AuthResponse(user.getId(), "Bearer", token);
  }

  public AuthDtos.AuthResponse login(AuthDtos.LoginRequest req) {
    UserEntity user = users.findByEmailIgnoreCase(req.email())
        .orElseThrow(() -> new ApiException(UNAUTHORIZED, "Invalid credentials"));

    if (!encoder.matches(req.password(), user.getPasswordHash())) {
      throw new ApiException(UNAUTHORIZED, "Invalid credentials");
    }

    String token = jwt.issue(user.getId(), user.getEmail());
    return new AuthDtos.AuthResponse(user.getId(), "Bearer", token);
  }
}
