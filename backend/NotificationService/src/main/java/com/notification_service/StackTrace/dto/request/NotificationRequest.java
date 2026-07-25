package com.notification_service.StackTrace.dto.request;

import com.notification_service.StackTrace.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {

    @NotNull
    private Long recipientId;

    @NotNull
    private Long senderId;

    @NotNull
    private NotificationType type;

    @NotBlank
    private String message;

    private Long referenceId;
}