package com.eventcollab.notification.controller;

import com.eventcollab.common.dto.ApiResponse;
import com.eventcollab.notification.dto.*;
import com.eventcollab.notification.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // -----------------------------------------------------------------------
    // REST — historique du chat
    // -----------------------------------------------------------------------

    // GET /api/v1/chat/{eventId}/history
    @GetMapping("/api/v1/chat/{eventId}/history")
    public ApiResponse<List<ChatMessageResponse>> history(@PathVariable UUID eventId) {
        return ApiResponse.ok(chatService.getHistory(eventId));
    }

    // POST /api/v1/chat/send — envoyer via REST (alternatif au WebSocket)
    @PostMapping("/api/v1/chat/send")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> send(
            @Valid @RequestBody ChatMessageRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Message envoye", chatService.sendMessage(req)));
    }

    // -----------------------------------------------------------------------
    // WebSocket — reception des messages via STOMP
    // Les clients envoient vers /app/chat.send
    // Le serveur broadcaste vers /topic/chat/{eventId}
    // -----------------------------------------------------------------------
    @MessageMapping("/chat.send")
    public void handleChatMessage(@Payload ChatMessageRequest req,
                                   @Header("Authorization") String authHeader) {
        // Le JwtAuthFilter a deja authentifie l utilisateur
        // On deligue simplement au service
        chatService.sendMessage(req);
    }
}