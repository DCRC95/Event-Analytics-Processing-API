package com.example.eventanalytics;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)

class AnalyticsIntegrationTest extends IntegrationTestBase {

  @Autowired
  TestRestTemplate rest;

  @Test
  void analytics_are_scoped_per_user_and_date_range_is_correct() {
    // 1) register/login 2 users
    String tokenA = registerAndLogin("a@test.com", "Password123");
    String tokenB = registerAndLogin("b@test.com", "Password123");

    // 2) create events for both users
    // Use explicit occurredAt around a known UTC day boundary.
    // 2026-01-10T10:00Z should fall into from=2026-01-10 to=2026-01-10 range.
    postEvent(tokenA, "TASK_CREATED", "TASK", "123", Instant.parse("2026-01-10T10:00:00Z"));
    postEvent(tokenA, "TASK_CREATED", "TASK", "123", Instant.parse("2026-01-10T11:00:00Z"));
    postEvent(tokenA, "TASK_COMPLETED", "TASK", "123", Instant.parse("2026-01-11T09:00:00Z"));

    postEvent(tokenB, "TASK_CREATED", "TASK", "999", Instant.parse("2026-01-10T12:00:00Z"));
    postEvent(tokenB, "TASK_CREATED", "TASK", "999", Instant.parse("2026-01-10T13:00:00Z"));

    // 3) User A summary for 2026-01-10..2026-01-10 should include only two events on that day
    ResponseEntity<Map> summaryA_0110 = getSummary(tokenA, "2026-01-10", "2026-01-10");
    assertThat(summaryA_0110.getStatusCode()).isEqualTo(HttpStatus.OK);

    Map bodyA = summaryA_0110.getBody();
    assertThat(bodyA).isNotNull();
    assertThat(((Number) bodyA.get("totalEvents")).longValue()).isEqualTo(2L);

    Map countsByTypeA = (Map) bodyA.get("countsByType");
    assertThat(((Number) countsByTypeA.get("TASK_CREATED")).longValue()).isEqualTo(2L);

    // dailyTotals should contain exactly 1 bucket: 2026-01-10 count=2
    var dailyTotalsA = (java.util.List<Map<String, Object>>) bodyA.get("dailyTotals");
    assertThat(dailyTotalsA).hasSize(1);
    assertThat(dailyTotalsA.get(0).get("date")).isEqualTo("2026-01-10");
    assertThat(((Number) dailyTotalsA.get(0).get("count")).longValue()).isEqualTo(2L);

    // 4) User B summary same range should be 2 events, but for entityId=999
    ResponseEntity<Map> summaryB_0110 = getSummary(tokenB, "2026-01-10", "2026-01-10");
    assertThat(summaryB_0110.getStatusCode()).isEqualTo(HttpStatus.OK);

    Map bodyB = summaryB_0110.getBody();
    assertThat(bodyB).isNotNull();
    assertThat(((Number) bodyB.get("totalEvents")).longValue()).isEqualTo(2L);

    // 5) Top endpoint: user A top for TASK_CREATED in 2026-01-10..2026-01-10 should return TASK/123 count=2
    ResponseEntity<Object[]> topA = getTop(tokenA, "TASK_CREATED", "2026-01-10", "2026-01-10", 5);
    assertThat(topA.getStatusCode()).isEqualTo(HttpStatus.OK);

    Object[] topListA = topA.getBody();
    assertThat(topListA).isNotNull();
    assertThat(topListA.length).isGreaterThanOrEqualTo(1);

    Map first = (Map) topListA[0];
    assertThat(first.get("entityType")).isEqualTo("TASK");
    assertThat(first.get("entityId")).isEqualTo("123");
    assertThat(((Number) first.get("count")).longValue()).isEqualTo(2L);
  }

  private String registerAndLogin(String email, String password) {
    // register
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    String registerJson = """
      {"email":"%s","password":"%s"}
    """.formatted(email, password);

    ResponseEntity<Map> reg = rest.exchange(
        "/auth/register",
        HttpMethod.POST,
        new HttpEntity<>(registerJson, headers),
        Map.class
    );
    assertThat(reg.getStatusCode()).isEqualTo(HttpStatus.OK);

    // login
    String loginJson = registerJson;
    ResponseEntity<Map> login = rest.exchange(
        "/auth/login",
        HttpMethod.POST,
        new HttpEntity<>(loginJson, headers),
        Map.class
    );
    assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);

    Map body = login.getBody();
    assertThat(body).isNotNull();
    assertThat(body.get("accessToken")).isNotNull();
    return body.get("accessToken").toString();
  }

  private void postEvent(String token, String type, String entityType, String entityId, Instant occurredAt) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(token);

    String json = """
      {
        "type":"%s",
        "entityType":"%s",
        "entityId":"%s",
        "occurredAt":"%s",
        "metadata":{"source":"itest"}
      }
    """.formatted(type, entityType, entityId, occurredAt.toString());

    ResponseEntity<Void> resp = rest.exchange(
        "/events",
        HttpMethod.POST,
        new HttpEntity<>(json, headers),
        Void.class
    );

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  private ResponseEntity<Map> getSummary(String token, String from, String to) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);

    return rest.exchange(
        "/analytics/summary?from=" + from + "&to=" + to,
        HttpMethod.GET,
        new HttpEntity<>(headers),
        Map.class
    );
  }

  private ResponseEntity<Object[]> getTop(String token, String type, String from, String to, int limit) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);

    String url = "/analytics/top?type=" + type + "&from=" + from + "&to=" + to + "&limit=" + limit;

    return rest.exchange(
        url,
        HttpMethod.GET,
        new HttpEntity<>(headers),
        Object[].class
    );
  }
}
