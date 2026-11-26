package com.stacktrace.post_service.client;

import com.stacktrace.post_service.dto.response.AuthorSummary;
import com.stacktrace.post_service.security.filter.InternalApiFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Calls user-service's internal API. Every call has timeouts and degrades gracefully. */
@Slf4j
@Component
public class UserClient {

    private final RestClient rest;

    public UserClient(@Value("${services.user.url}") String baseUrl,
                      @Value("${internal.api-token}") String internalToken) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(3));
        this.rest = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .defaultHeader(InternalApiFilter.HEADER, internalToken)
                .build();
    }

    /** Resolves author summaries; unknown/unreachable ids map to a placeholder author. */
    public Map<Long, AuthorSummary> findAuthors(Collection<Long> ids) {
        Set<Long> unique = new LinkedHashSet<>(ids);
        unique.remove(null);
        if (unique.isEmpty()) {
            return Map.of();
        }
        Map<Long, AuthorSummary> result = new HashMap<>();
        try {
            List<AuthorSummary> found = rest.get()
                    .uri(u -> u.path("/internal/users/batch").queryParam("ids", unique.toArray()).build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<AuthorSummary>>() {});
            if (found != null) {
                result.putAll(found.stream().collect(Collectors.toMap(AuthorSummary::id, Function.identity())));
            }
        } catch (RestClientException e) {
            log.warn("user-service batch lookup failed: {}", e.getMessage());
        }
        unique.forEach(id -> result.putIfAbsent(id, AuthorSummary.unknown(id)));
        return result;
    }

    public List<Long> followingIds(Long userId) {
        List<Long> ids = rest.get()
                .uri("/internal/users/{id}/following-ids", userId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Long>>() {});
        return ids == null ? List.of() : ids;
    }

    /** Best effort: gamification must never break the main request. */
    public void addPoints(Long userId, int delta) {
        try {
            rest.post()
                    .uri("/internal/users/{id}/points", userId)
                    .body(Map.of("delta", delta))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.warn("Could not award {} points to user {}: {}", delta, userId, e.getMessage());
        }
    }
}
