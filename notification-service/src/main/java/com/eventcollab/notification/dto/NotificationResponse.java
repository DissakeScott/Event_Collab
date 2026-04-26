package com.eventcollab.notification.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class NotificationResponse {
    private UUID          id;
    private String        type;
    private String        title;
    private String        message;
    private boolean       read;
    private LocalDateTime createdAt;
}