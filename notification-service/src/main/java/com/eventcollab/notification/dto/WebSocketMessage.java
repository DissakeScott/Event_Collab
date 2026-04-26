package com.eventcollab.notification.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// Message envoyé via WebSocket aux clients connectés
@Getter
@Builder
public class WebSocketMessage {
    private String        type;      // "CHAT" ou "NOTIFICATION"
    private String        eventId;
    private String        userId;
    private String        userEmail;
    private String        content;
    private LocalDateTime timestamp;
}