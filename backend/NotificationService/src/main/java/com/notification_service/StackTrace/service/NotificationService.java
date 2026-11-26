package com.notification_service.StackTrace.service;

import com.notification_service.StackTrace.dto.request.NotificationRequest;
import com.notification_service.StackTrace.dto.response.NotificationResponse;
import com.notification_service.StackTrace.dto.response.PageResponse;

public interface NotificationService {

    NotificationResponse createNotification(NotificationRequest request);

    PageResponse<NotificationResponse> getNotifications(Long recipientId, int page, int size);

    long unreadCount(Long recipientId);

    NotificationResponse markAsRead(Long notificationId, Long recipientId);

    void markAllAsRead(Long recipientId);

    void deleteNotification(Long notificationId, Long recipientId);
}
