package com.example.eventanalytics.controller;

import com.example.eventanalytics.dto.AuthDtos;
import com.example.eventanalytics.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final AuthService auth;

  public AuthController(AuthService auth) {
    this.auth = auth;
  }

  @PostMapping("/register")
  public AuthDtos.AuthResponse register(@Valid @RequestBody AuthDtos.RegisterRequest req) {
    return auth.register(req);
  }

  @PostMapping("/login")
  public AuthDtos.AuthResponse login(@Valid @RequestBody AuthDtos.LoginRequest req) {
    return auth.login(req);
  }
}
