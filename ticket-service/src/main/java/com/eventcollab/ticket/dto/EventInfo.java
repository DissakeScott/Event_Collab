package com.eventcollab.ticket.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

// Representation minimale d un evenement
// recue depuis le event-service via HTTP
@Data
public class EventInfo {
    private UUID          id;
    private String        title;
    private String        status;
    private int           maxCapacity;
    private int           currentCapacity;
    private boolean       isFull;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private UUID          organizerId;
}