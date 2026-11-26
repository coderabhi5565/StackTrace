package com.StackTrace.User_Service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Sends FOLLOW notifications after the follow commits. Best effort: failures are only logged. */
@Slf4j
@Component
public class NotificationClient {

    private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private final RestClient rest;

    public NotificationClient(@Value("${services.notification.url}") String baseUrl,
                              @Value("${internal.api-token}") String token) {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(Duration.ofSeconds(2));
        f.setReadTimeout(Duration.ofSeconds(3));
        rest = RestClient.builder().baseUrl(baseUrl).requestFactory(f)
                .defaultHeader("X-Internal-Token", token).build();
    }

    public void followedAfterCommit(Long followerId, Long followedId) {
        Runnable send = () -> {
            try {
                rest.post().uri("/internal/notifications")
                        .body(Map.of("recipientId", followedId, "senderId", followerId, "type", "FOLLOW",
                                "message", "started following you.", "referenceId", followerId))
                        .retrieve().toBodilessEntity();
            } catch (RuntimeException e) {
                log.warn("Follow notification failed: {}", e.getMessage());
            }
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    EXECUTOR.submit(send);
                }
            });
        } else {
            EXECUTOR.submit(send);
        }
    }
}
