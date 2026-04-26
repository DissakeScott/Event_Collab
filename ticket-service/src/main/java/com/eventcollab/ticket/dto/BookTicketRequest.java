package com.eventcollab.ticket.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class BookTicketRequest {

    @NotNull(message = "L identifiant de l evenement est obligatoire")
    private UUID eventId;
}