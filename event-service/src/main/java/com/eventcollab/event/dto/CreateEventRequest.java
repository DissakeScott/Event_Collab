package com.eventcollab.event.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateEventRequest {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 150, message = "Titre trop long")
    private String title;

    private String description;

    @NotBlank(message = "Le lieu est obligatoire")
    private String location;

    @NotNull(message = "La date de debut est obligatoire")
    @Future(message = "La date de debut doit etre dans le futur")
    private LocalDateTime startDate;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime endDate;

    @Min(value = 1, message = "La capacite doit etre au moins 1")
    @Max(value = 100000, message = "Capacite trop grande")
    private int maxCapacity;
}