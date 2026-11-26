package com.stacktrace.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Rate-limit buckets. Authenticated requests are keyed by the (signature-verified) user id so users behind
 * one NAT/BFF do not share a bucket; anonymous requests are keyed by client IP.
 *
 * Client IP: the first X-Forwarded-For hop (set by the frontend BFF / platform proxy), else the socket address.
 */
@Configuration
public class RateLimitKeys {

    private final SecretKey key;

    public RateLimitKeys(@Value("${jwt.secret}") String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    /** Default: per user when logged in, per IP otherwise. */
    @Bean
    @Primary
    KeyResolver userOrIpKeyResolver() {
        return exchange -> Mono.just(userId(exchange).map(id -> "u:" + id).orElseGet(() -> "ip:" + clientIp(exchange)));
    }

    /** Auth endpoints: always per IP (brute-force protection must not depend on a token). */
    @Bean
    KeyResolver clientIpKeyResolver() {
        return exchange -> Mono.just("auth-ip:" + clientIp(exchange));
    }

    static String clientIp(ServerWebExchange exchange) {
        String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        InetSocketAddress remote = exchange.getRequest().getRemoteAddress();
        return remote == null || remote.getAddress() == null ? "unknown" : remote.getAddress().getHostAddress();
    }

    private java.util.Optional<String> userId(ServerWebExchange exchange) {
        String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return java.util.Optional.empty();
        }
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(header.substring(7)).getPayload();
            Object id = claims.get("userId");
            return java.util.Optional.ofNullable(id).map(String::valueOf);
        } catch (JwtException | IllegalArgumentException e) {
            return java.util.Optional.empty();
        }
    }
}
