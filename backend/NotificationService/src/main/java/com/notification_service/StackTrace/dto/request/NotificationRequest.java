package com.notification_service.StackTrace.dto.request;

import com.notification_service.StackTrace.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(max = 255)
    private String message;

    private Long referenceId;
}
