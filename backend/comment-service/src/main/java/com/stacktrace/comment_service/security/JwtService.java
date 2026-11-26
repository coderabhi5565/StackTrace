package com.stacktrace.comment_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/** Stateless validation of access tokens issued by user-service. */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecretKey;

    private SecretKey signingKey;

    @PostConstruct
    void init() {
        byte[] bytes = jwtSecretKey.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 bytes");
        }
        signingKey = Keys.hmacShaKeyFor(bytes);
    }

    public Optional<Long> validateAndGetUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer("stacktrace")
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            if (!"access".equals(claims.get("typ", String.class))) {
                return Optional.empty();
            }
            return Optional.ofNullable(claims.get("userId", Number.class)).map(Number::longValue);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
