package com.example.eventanalytics.config;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.eventanalytics.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwt;

  public JwtAuthFilter(JwtService jwt) {
    this.jwt = jwt;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getServletPath();
    return path.startsWith("/auth/")
        || path.equals("/health")
        || path.equals("/error")
        || path.startsWith("/v3/api-docs")
        || path.startsWith("/swagger-ui")
        || path.equals("/swagger-ui.html");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain
  ) throws ServletException, IOException {

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
          userId, // principal (we’ll use this for scoping)
          null,
          List.of(new SimpleGrantedAuthority("ROLE_USER"))
      );

      SecurityContextHolder.getContext().setAuthentication(auth);
      chain.doFilter(request, response);

    } catch (JwtException | IllegalArgumentException e) {
      // Invalid token: clear context and continue. Protected endpoints will 401.
      SecurityContextHolder.clearContext();
      chain.doFilter(request, response);
    }
  }
}
