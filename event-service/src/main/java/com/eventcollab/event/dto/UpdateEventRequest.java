package com.eventcollab.event.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateEventRequest {

    @Size(max = 150)
    private String title;

    private String description;

    private String location;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Min(1)
    private Integer maxCapacity;
}