package com.notification_service.StackTrace.service.impl;

import com.notification_service.StackTrace.dto.request.NotificationRequest;
import com.notification_service.StackTrace.dto.response.NotificationResponse;
import com.notification_service.StackTrace.exception.NotificationNotFoundException;
import com.notification_service.StackTrace.service.NotificationService;
import com.notification_service.StackTrace.entity.Notification;
import com.notification_service.StackTrace.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public NotificationResponse createNotification(NotificationRequest request) {

        Notification notification = Notification.builder()
                .recipientId(request.getRecipientId())
                .senderId(request.getSenderId())
                .type(request.getType())
                .message(request.getMessage())
                .referenceId(request.getReferenceId())
                .build();

        notification = notificationRepository.save(notification);

        return mapToResponse(notification);
    }

    @Override
    public List<NotificationResponse> getNotifications(Long recipientId) {

        return notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(recipientId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public NotificationResponse markAsRead(Long notificationId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found"));

        notification.setIsRead(true);

        notificationRepository.save(notification);

        return mapToResponse(notification);
    }

    @Override
    public void markAllAsRead(Long recipientId) {

        List<Notification> notifications =
                notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId);

        notifications.forEach(notification -> notification.setIsRead(true));

        notificationRepository.saveAll(notifications);
    }

    @Override
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    private NotificationResponse mapToResponse(Notification notification) {

        return NotificationResponse.builder()
                .id(notification.getId())
                .recipientId(notification.getRecipientId())
                .senderId(notification.getSenderId())
                .type(notification.getType())
                .message(notification.getMessage())
                .referenceId(notification.getReferenceId())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}