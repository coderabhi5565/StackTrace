package com.stacktrace.comment_service.dto.request;

import com.stacktrace.comment_service.enums.NotificationType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {

    private Long recipientId;

    private Long senderId;

    private NotificationType type;

    private String message;

    private Long referenceId;

}