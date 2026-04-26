package com.eventcollab.ticket.client;

import com.eventcollab.common.dto.ApiResponse;
import com.eventcollab.common.exception.BusinessException;
import com.eventcollab.ticket.dto.EventInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.UUID;

@Component
@Slf4j
public class EventServiceClient {

    private final WebClient webClient;

    public EventServiceClient(@Value("${app.event-service.url}") String eventServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(eventServiceUrl)
                .build();
    }

    // Recuperer les infos d un evenement depuis le event-service
    public EventInfo getEvent(UUID eventId) {
        try {
            ApiResponse<EventInfo> response = webClient.get()
                    .uri("/api/v1/events/{id}", eventId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<EventInfo>>() {})
                    .block();

            if (response == null || response.getData() == null) {
                throw BusinessException.notFound("Evenement introuvable : " + eventId);
            }

            return response.getData();

        } catch (WebClientResponseException.NotFound e) {
            throw BusinessException.notFound("Evenement introuvable : " + eventId);
        } catch (WebClientResponseException e) {
            log.error("Erreur event-service : {}", e.getMessage());
            throw BusinessException.badRequest("Impossible de contacter le service evenement");
        }
    }


    public void incrementCapacity(UUID eventId) {
    try {
        webClient.post()
                .uri("/api/v1/events/{id}/capacity/increment", eventId)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        log.debug("Capacite incrementee pour l event {}", eventId);
    } catch (WebClientResponseException e) {
        log.error("Erreur increment capacite : {}", e.getMessage());
        throw BusinessException.badRequest("Impossible de mettre a jour la capacite");
    }
}

public void decrementCapacity(UUID eventId) {
    try {
        webClient.post()
                .uri("/api/v1/events/{id}/capacity/decrement", eventId)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        log.debug("Capacite decrementee pour l event {}", eventId);
    } catch (WebClientResponseException e) {
        log.error("Erreur decrement capacite : {}", e.getMessage());
    }
}
}