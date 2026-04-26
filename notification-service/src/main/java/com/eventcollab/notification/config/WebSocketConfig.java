package com.eventcollab.notification.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefixe des topics publics (chat)
        // Les clients s abonnent a /topic/chat/{eventId}
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefixe des messages envoyes par les clients vers le serveur
        // Les clients envoient vers /app/chat.send
        registry.setApplicationDestinationPrefixes("/app");

        // Prefixe pour les messages personnels (notifications)
        // /user/{userId}/queue/notifications
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Point d entree WebSocket
        // Les clients se connectent a ws://localhost:8084/ws
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();   // fallback pour les navigateurs sans WebSocket
    }
}