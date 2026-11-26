package com.notification_service.StackTrace.client;

import com.notification_service.StackTrace.dto.response.ActorSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.*;

/** Resolves sender names via user-service's internal batch endpoint (best effort). */
@Slf4j
@Component
public class UserClient {

    private final RestClient rest;

    public UserClient(@Value("${user-service.url}") String baseUrl,
                      @Value("${internal.api-token}") String token) {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(Duration.ofSeconds(2));
        f.setReadTimeout(Duration.ofSeconds(3));
        rest = RestClient.builder().baseUrl(baseUrl).requestFactory(f)
                .defaultHeader("X-Internal-Token", token).build();
    }

    public Map<Long, ActorSummary> findUsers(Collection<Long> ids) {
        Set<Long> unique = new HashSet<>(ids);
        unique.remove(null);
        if (unique.isEmpty()) {
            return Map.of();
        }
        try {
            List<ActorSummary> users = rest.get()
                    .uri(u -> u.path("/internal/users/batch").queryParam("ids", unique.toArray()).build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ActorSummary>>() {});
            Map<Long, ActorSummary> map = new HashMap<>();
            if (users != null) {
                users.forEach(u -> map.put(u.id(), u));
            }
            return map;
        } catch (RestClientException e) {
            log.warn("user-service lookup failed: {}", e.getMessage());
            return Map.of();
        }
    }
}
