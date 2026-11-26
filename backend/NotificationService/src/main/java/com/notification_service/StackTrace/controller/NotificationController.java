package com.notification_service.StackTrace.controller;

import com.notification_service.StackTrace.dto.request.NotificationRequest;
import com.notification_service.StackTrace.dto.response.NotificationResponse;
import com.notification_service.StackTrace.dto.response.PageResponse;
import com.notification_service.StackTrace.dto.response.UnreadCountResponse;
import com.notification_service.StackTrace.security.CurrentUser;
import com.notification_service.StackTrace.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** Internal: called by other services (X-Internal-Token). */
    @PostMapping("/internal/notifications")
    public ResponseEntity<NotificationResponse> createNotification(@Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.createNotification(request));
    }

    @GetMapping({"/api/notifications", "/api/v1/notifications"})
    public PageResponse<NotificationResponse> getNotifications(@RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "20") int size) {
        return notificationService.getNotifications(CurrentUser.id(), page, size);
    }

    @GetMapping({"/api/notifications/unread-count", "/api/v1/notifications/unread-count"})
    public UnreadCountResponse unreadCount() {
        return new UnreadCountResponse(notificationService.unreadCount(CurrentUser.id()));
    }

    @PutMapping({"/api/notifications/{id}/read", "/api/v1/notifications/{id}/read"})
    public NotificationResponse markAsRead(@PathVariable Long id) {
        return notificationService.markAsRead(id, CurrentUser.id());
    }

    @PutMapping({"/api/notifications/read-all", "/api/v1/notifications/read-all"})
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead(CurrentUser.id());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping({"/api/notifications/{id}", "/api/v1/notifications/{id}"})
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id, CurrentUser.id());
        return ResponseEntity.noContent().build();
    }
}
