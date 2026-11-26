package com.StackTrace.User_Service.Service;

import com.StackTrace.User_Service.Repository.RefreshTokenRepository;
import com.StackTrace.User_Service.exception.InvalidTokenException;
import com.StackTrace.User_Service.model.RefreshToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Opaque, rotating refresh tokens. Each refresh revokes the presented token and issues a new one;
 * presenting an already-revoked token is treated as theft and revokes every session of that user.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository repository;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    public record Rotation(Long userId, String newToken) {
    }

    @Transactional
    public String issue(Long userId) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        repository.save(RefreshToken.builder()
                .userId(userId)
                .tokenHash(hash(token))
                .expiresAt(Instant.now().plusMillis(refreshExpirationMs))
                .build());
        return token;
    }

    @Transactional(noRollbackFor = InvalidTokenException.class)
    public Rotation rotate(String presented) {
        RefreshToken stored = repository.findByTokenHash(hash(presented))
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));
        Instant now = Instant.now();
        if (stored.getRevokedAt() != null) {
            log.warn("Refresh token reuse detected for user {}; revoking all sessions", stored.getUserId());
            repository.revokeAllForUser(stored.getUserId(), now);
            throw new InvalidTokenException("Invalid refresh token");
        }
        if (stored.getExpiresAt().isBefore(now)) {
            throw new InvalidTokenException("Refresh token expired");
        }
        stored.setRevokedAt(now);
        return new Rotation(stored.getUserId(), issue(stored.getUserId()));
    }

    @Transactional
    public void revoke(String presented) {
        repository.findByTokenHash(hash(presented)).ifPresent(t -> {
            if (t.getRevokedAt() == null) {
                t.setRevokedAt(Instant.now());
            }
        });
    }

    @Scheduled(cron = "0 17 3 * * *")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void purgeExpired() {
        int n = repository.deleteExpired(Instant.now().minus(Duration.ofDays(1)));
        if (n > 0) {
            log.info("Purged {} expired refresh tokens", n);
        }
    }

    static String hash(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
