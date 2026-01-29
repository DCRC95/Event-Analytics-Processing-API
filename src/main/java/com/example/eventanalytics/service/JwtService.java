package com.example.eventanalytics.service;

import com.example.eventanalytics.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

  private final JwtProperties props;
  private final SecretKey key;

  public JwtService(JwtProperties props) {
    this.props = props;
    // Must be >= 32 chars for HS256 safety (we enforce via secret length expectation in .env.example)
    this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
  }

  public String issue(UUID userId, String email) {
    Instant now = Instant.now();
    Instant exp = now.plus(props.expMinutes(), ChronoUnit.MINUTES);

    return Jwts.builder()
        .issuer(props.issuer())
        .subject(userId.toString())
        .claim("email", email)
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .signWith(key)
        .compact();
  }

  public Jws<Claims> parseAndValidate(String token) throws JwtException {
    return Jwts.parser()
        .verifyWith(key)
        .requireIssuer(props.issuer())
        .build()
        .parseSignedClaims(token);
  }
}
