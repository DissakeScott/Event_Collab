package com.eventcollab.notification.service;

import com.eventcollab.notification.domain.ChatMessage;
import com.eventcollab.notification.dto.*;
import com.eventcollab.notification.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // -----------------------------------------------------------------------
    // ENVOYER un message dans le chat d un event
    // -----------------------------------------------------------------------
    @Transactional
    public ChatMessageResponse sendMessage(ChatMessageRequest req) {
        Authentication auth   = SecurityContextHolder.getContext().getAuthentication();
        UUID   userId         = UUID.fromString((String) auth.getCredentials());
        String userEmail      = (String) auth.getPrincipal();

        // 1. Sauvegarder le message en base
        ChatMessage message = ChatMessage.builder()
                .eventId(req.getEventId())
                .userId(userId)
                .userEmail(userEmail)
                .content(req.getContent())
                .build();

        chatMessageRepository.save(message);

        // 2. Broadcaster a tous les clients connectes a cet event
        // Les clients ecoutent sur /topic/chat/{eventId}
        WebSocketMessage wsMessage = WebSocketMessage.builder()
                .type("CHAT")
                .eventId(req.getEventId().toString())
                .userId(userId.toString())
                .userEmail(userEmail)
                .content(req.getContent())
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend(
                "/topic/chat/" + req.getEventId(),
                wsMessage
        );

        log.info("Message chat envoye : event={} user={}", req.getEventId(), userEmail);
        return toResponse(message);
    }

    // -----------------------------------------------------------------------
    // HISTORIQUE du chat d un event (50 derniers messages)
    // -----------------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getHistory(UUID eventId) {
        return chatMessageRepository
                .findTop50ByEventIdOrderByCreatedAtDesc(eventId)
                .stream().map(this::toResponse).toList();
    }

    private ChatMessageResponse toResponse(ChatMessage m) {
        return ChatMessageResponse.builder()
                .id(m.getId())
                .eventId(m.getEventId())
                .userId(m.getUserId())
                .userEmail(m.getUserEmail())
                .content(m.getContent())
                .createdAt(m.getCreatedAt())
                .build();
    }
}