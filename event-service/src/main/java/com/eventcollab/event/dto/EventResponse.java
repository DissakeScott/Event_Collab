package com.eventcollab.event.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class EventResponse {
    private UUID          id;
    private String        title;
    private String        description;
    private String        location;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int           maxCapacity;
    private int           currentCapacity;
    private int           availableSpots;
    private boolean       isFull;
    private String        status;
    private UUID          organizerId;
    private String        organizerEmail;
    private LocalDateTime createdAt;
}