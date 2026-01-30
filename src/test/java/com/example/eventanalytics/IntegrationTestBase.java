package com.example.eventanalytics;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class IntegrationTestBase {

  @Container
  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
      .withDatabaseName("event_analytics")
      .withUsername("app")
      .withPassword("app");

  @DynamicPropertySource
  static void registerProps(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);

    // Ensure Flyway runs against the container DB
    registry.add("spring.flyway.enabled", () -> "true");

    // Make sure Hibernate doesn't try to create/modify schema (Flyway owns schema)
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");

    // JWT settings for tests (keep stable so tokens validate)
    registry.add("app.jwt.secret", () -> "test-secret-at-least-32-chars-long-123456");
    registry.add("app.jwt.issuer", () -> "event-analytics-api");
    registry.add("app.jwt.exp-minutes", () -> "60");
  }
}

