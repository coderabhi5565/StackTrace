package com.notification_service.StackTrace.service;

import com.notification_service.StackTrace.dto.request.NotificationRequest;
import com.notification_service.StackTrace.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationService {

    NotificationResponse createNotification(NotificationRequest request);

    List<NotificationResponse> getNotifications(Long recipientId);

    NotificationResponse markAsRead(Long notificationId);

    void markAllAsRead(Long recipientId);

    void deleteNotification(Long notificationId);

}