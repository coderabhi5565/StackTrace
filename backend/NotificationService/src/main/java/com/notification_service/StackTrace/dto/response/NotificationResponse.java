package com.notification_service.StackTrace.dto.response;

import com.notification_service.StackTrace.enums.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;

    private Long recipientId;

    private Long senderId;

    private NotificationType type;

    private String message;

    private Long referenceId;

    private Boolean isRead;

    private LocalDateTime createdAt;
}