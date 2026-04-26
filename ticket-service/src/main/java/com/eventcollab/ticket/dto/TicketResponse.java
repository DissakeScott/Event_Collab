package com.eventcollab.ticket.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class TicketResponse {
    private UUID          id;
    private UUID          eventId;
    private UUID          userId;
    private String        userEmail;
    private String        status;
    private String        qrCode;       // base64 PNG
    private LocalDateTime bookedAt;
    private LocalDateTime cancelledAt;
}