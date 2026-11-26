package com.notification_service.StackTrace.service.impl;

import com.notification_service.StackTrace.client.UserClient;
import com.notification_service.StackTrace.dto.request.NotificationRequest;
import com.notification_service.StackTrace.dto.response.ActorSummary;
import com.notification_service.StackTrace.dto.response.NotificationResponse;
import com.notification_service.StackTrace.dto.response.PageResponse;
import com.notification_service.StackTrace.entity.Notification;
import com.notification_service.StackTrace.exception.NotificationNotFoundException;
import com.notification_service.StackTrace.repository.NotificationRepository;
import com.notification_service.StackTrace.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/** Every user-facing operation is scoped to the authenticated recipient (no ids from the client are trusted). */
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private static final int MAX_PAGE_SIZE = 50;

    private final NotificationRepository notificationRepository;
    private final UserClient userClient;

    @Override
    public NotificationResponse createNotification(NotificationRequest request) {
        Notification notification = Notification.builder()
                .recipientId(request.getRecipientId())
                .senderId(request.getSenderId())
                .type(request.getType())
                .message(request.getMessage())
                .referenceId(request.getReferenceId())
                .build();
        return mapToResponse(notificationRepository.save(notification), Map.of());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getNotifications(Long recipientId, int page, int size) {
        Page<Notification> result = notificationRepository.findByRecipientIdOrderByCreatedAtDescIdDesc(recipientId,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE)));
        Map<Long, ActorSummary> senders = userClient.findUsers(
                result.getContent().stream().map(Notification::getSenderId).toList());
        return new PageResponse<>(result.getContent().stream().map(n -> mapToResponse(n, senders)).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages(),
                result.isLast());
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount(Long recipientId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(recipientId);
    }

    @Override
    public NotificationResponse markAsRead(Long notificationId, Long recipientId) {
        Notification notification = owned(notificationId, recipientId);
        notification.setIsRead(true);
        return mapToResponse(notificationRepository.save(notification), Map.of());
    }

    @Override
    public void markAllAsRead(Long recipientId) {
        notificationRepository.markAllRead(recipientId);
    }

    @Override
    public void deleteNotification(Long notificationId, Long recipientId) {
        notificationRepository.delete(owned(notificationId, recipientId));
    }

    /** Someone else's notification is reported as "not found" so ids cannot be probed. */
    private Notification owned(Long id, Long recipientId) {
        return notificationRepository.findByIdAndRecipientId(id, recipientId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found"));
    }

    private NotificationResponse mapToResponse(Notification notification, Map<Long, ActorSummary> senders) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .recipientId(notification.getRecipientId())
                .senderId(notification.getSenderId())
                .sender(senders.get(notification.getSenderId()))
                .type(notification.getType())
                .message(notification.getMessage())
                .referenceId(notification.getReferenceId())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt() == null ? null
                        : notification.getCreatedAt().toInstant(java.time.ZoneOffset.UTC))
                .build();
    }
}
