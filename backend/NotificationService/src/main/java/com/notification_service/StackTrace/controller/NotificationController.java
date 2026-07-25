package com.notification_service.StackTrace.controller;

import com.notification_service.StackTrace.dto.request.NotificationRequest;
import com.notification_service.StackTrace.dto.response.NotificationResponse;
import com.notification_service.StackTrace.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/internal/notifications")
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody NotificationRequest request
    ) {
        return ResponseEntity.ok(
                notificationService.createNotification(request)
        );
    }

    @GetMapping("/api/v1/notifications")
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @RequestParam Long recipientId
    ) {
        return ResponseEntity.ok(
                notificationService.getNotifications(recipientId)
        );
    }

    @PutMapping("/api/v1/notifications/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                notificationService.markAsRead(id)
        );
    }

    @PutMapping("/api/v1/notifications/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @RequestParam Long recipientId
    ) {
        notificationService.markAllAsRead(recipientId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/v1/notifications/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id
    ) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}