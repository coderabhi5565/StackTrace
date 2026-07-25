package com.stacktrace.comment_service.client;

import com.stacktrace.comment_service.dto.request.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "notification-service",
        url = "${notification-service.url}"
)
public interface NotificationClient {

    @PostMapping("/internal/notifications")
    void createNotification(
            @RequestBody NotificationRequest request
    );
}