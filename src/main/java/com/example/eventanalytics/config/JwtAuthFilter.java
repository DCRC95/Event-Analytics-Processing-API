package com.example.eventanalytics.config;

import com.example.eventanalytics.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwt;

  public JwtAuthFilter(JwtService jwt) {
    this.jwt = jwt;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header == null || !header.startsWith("Bearer ")) {
      chain.doFilter(request, response);
      return;
    }

    String token = header.substring("Bearer ".length()).trim();
    try {
      Jws<Claims> parsed = jwt.parseAndValidate(token);
      UUID userId = UUID.fromString(parsed.getPayload().getSubject());

      var auth = new UsernamePasswordAuthenticationToken(
          userId, // principal
          null,
          List.of(new SimpleGrantedAuthority("ROLE_USER"))
      );
      SecurityContextHolder.getContext().setAuthentication(auth);
      chain.doFilter(request, response);
    } catch (JwtException | IllegalArgumentException e) {
      // invalid token -> no auth; downstream will return 401 for protected endpoints
      SecurityContextHolder.clearContext();
      chain.doFilter(request, response);
    }
  }
}
