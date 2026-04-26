package com.eventcollab.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class ChatMessageRequest {

    @NotNull(message = "eventId obligatoire")
    private UUID eventId;

    @NotBlank(message = "Le message ne peut pas etre vide")
    @Size(max = 1000, message = "Message trop long")
    private String content;
}