package com.stacktrace.post_service.security.service;

import io.jsonwebtoken.Claims;

public interface JwtService {

    Long extractUserId(String token);

    boolean isTokenValid(String token);

    Claims extractAllClaims(String token);

    String extractEmail(String token);
}