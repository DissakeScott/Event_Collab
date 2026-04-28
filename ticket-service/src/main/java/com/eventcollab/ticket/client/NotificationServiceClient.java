package com.eventcollab.ticket.client;

import com.eventcollab.ticket.dto.NotificationRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@Slf4j
public class NotificationServiceClient {

    private final WebClient webClient;

    public NotificationServiceClient(@Value("${app.notification-service.url:http://localhost:8084}") String notificationUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(notificationUrl)
                .build();
    }

    // Récupération sécurisée du token JWT de la requête en cours
    private String getAuthToken() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getHeader(HttpHeaders.AUTHORIZATION);
        }
        return null;
    }

    public void send(NotificationRequest request) {
        try {
            webClient.post()
                    .uri("/api/v1/notifications/send")
                    .header(HttpHeaders.AUTHORIZATION, getAuthToken()) // On passe le badge de sécurité !
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
            log.debug("Notification relayée avec succès au notification-service !");
        } catch (WebClientResponseException e) {
            log.error("Erreur de sécurité ou serveur depuis notification-service : {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Impossible de joindre le notification-service : {}", e.getMessage());
        }
    }
}