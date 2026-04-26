package com.eventcollab.notification.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ChatMessageResponse {
    private UUID          id;
    private UUID          eventId;
    private UUID          userId;
    private String        userEmail;
    private String        content;
    private LocalDateTime createdAt;
}