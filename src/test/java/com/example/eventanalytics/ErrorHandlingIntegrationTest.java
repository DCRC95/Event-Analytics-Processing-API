package com.example.eventanalytics;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ErrorHandlingIntegrationTest extends IntegrationTestBase {

  @Autowired TestRestTemplate rest;

  @Test
  void validation_error_returns_consistent_shape() {
    String email = "v+" + System.nanoTime() + "@test.com";
    String token = registerAndLogin(email, "Password123");
    

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(token);

    // missing entityId => validation should fail
    String json = """
      {"type":"TASK_CREATED","entityType":"TASK"}
    """;

    ResponseEntity<Map> resp = rest.exchange(
        "/events",
        HttpMethod.POST,
        new HttpEntity<>(json, headers),
        Map.class
    );

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    Map body = resp.getBody();
    assertThat(body).isNotNull();
    assertThat(body.get("status")).isEqualTo(400);
    assertThat(body.get("path")).isEqualTo("/events");
    assertThat(body.get("validationErrors")).isNotNull();
  }

  private String registerAndLogin(String email, String password) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
  
    String json = """
      {"email":"%s","password":"%s"}
    """.formatted(email, password);
  
    // Register (might be OK or CONFLICT if already exists)
    ResponseEntity<Map> reg = rest.exchange(
        "/auth/register",
        HttpMethod.POST,
        new HttpEntity<>(json, headers),
        Map.class
    );
    assertThat(reg.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.CONFLICT);
  
    // Login must be OK
    ResponseEntity<Map> login = rest.exchange(
        "/auth/login",
        HttpMethod.POST,
        new HttpEntity<>(json, headers),
        Map.class
    );
  
    assertThat(login.getStatusCode())
        .withFailMessage("Login failed. status=%s body=%s", login.getStatusCode(), login.getBody())
        .isEqualTo(HttpStatus.OK);
  
    Map body = login.getBody();
    assertThat(body)
        .withFailMessage("Login body was null")
        .isNotNull();
  
    // Accept either field name to match your DTO
    Object token = body.get("accessToken");
    if (token == null) token = body.get("token");
    if (token == null) token = body.get("jwt");
  
    assertThat(token)
        .withFailMessage("Token field missing. keys=%s fullBody=%s", body.keySet(), body)
        .isNotNull();
  
    return token.toString();
  }
  
}

