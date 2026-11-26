package com.stacktrace.post_service.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/** Stateless validation of access tokens issued by user-service (shared JWT_SECRET). */
@Service
public class JwtServiceImpl implements JwtService {

    private static final String ISSUER = "stacktrace";

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 bytes");
        }
        signingKey = Keys.hmacShaKeyFor(bytes);
    }

    @Override
    public Long extractUserId(String token) {
        Number id = extractAllClaims(token).get("userId", Number.class);
        return id == null ? null : id.longValue();
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return "access".equals(claims.get("typ", String.class)) && claims.get("userId") != null;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(ISSUER)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }
}
