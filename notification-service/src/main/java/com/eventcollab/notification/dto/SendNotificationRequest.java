package com.eventcollab.notification.dto;

import com.eventcollab.notification.domain.NotificationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class SendNotificationRequest {

    @NotNull(message = "userId obligatoire")
    private UUID userId;

    @NotBlank @Email
    private String userEmail;

    @NotNull
    private NotificationType type;

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    private boolean sendEmail = true;
}