package com.eventcollab.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private UUID userId;
    private String userEmail;
    private String type; // ex: "TICKET_BOOKED"
    private String title;
    private String message;
    private boolean sendEmail;
}