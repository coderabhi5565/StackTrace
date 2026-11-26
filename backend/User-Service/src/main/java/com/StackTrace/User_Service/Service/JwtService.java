package com.StackTrace.User_Service.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

/**
 * Issues and validates short-lived HS256 access tokens. The same secret (JWT_SECRET) is shared
 * with every other service, which validate tokens statelessly.
 */
@Service
public class JwtService {

    public static final String ISSUER = "stacktrace";

    @Value("${jwt.secret}")
    private String jwtSecretKey;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey signingKey;

    @PostConstruct
    void init() {
        byte[] bytes = jwtSecretKey.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 bytes");
        }
        signingKey = Keys.hmacShaKeyFor(bytes);
    }

    public String generateToken(Long userId, String username) {
        Date now = new Date();
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(String.valueOf(userId))
                .claim("userId", userId)
                .claim("username", username)
                .claim("typ", "access")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(signingKey)
                .compact();
    }

    public long getExpirationSeconds() {
        return expiration / 1000;
    }

    /** Returns the user id if the token is a valid, unexpired access token. */
    public Optional<Long> validateAndGetUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(ISSUER)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            if (!"access".equals(claims.get("typ", String.class))) {
                return Optional.empty();
            }
            Number id = claims.get("userId", Number.class);
            return Optional.ofNullable(id).map(Number::longValue);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
