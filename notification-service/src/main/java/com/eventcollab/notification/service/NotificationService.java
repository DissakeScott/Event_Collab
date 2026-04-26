package com.eventcollab.notification.service;

import com.eventcollab.common.exception.BusinessException;
import com.eventcollab.notification.domain.Notification;
import com.eventcollab.notification.dto.*;
import com.eventcollab.notification.repository.NotificationRepository;
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
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService           emailService;
    private final SimpMessagingTemplate  messagingTemplate;  // WebSocket

    // -----------------------------------------------------------------------
    // ENVOYER une notification (email + in-app + WebSocket)
    // -----------------------------------------------------------------------
    @Transactional
    public NotificationResponse send(SendNotificationRequest req) {

        // 1. Sauvegarder en base (notification in-app)
        Notification notification = Notification.builder()
                .userId(req.getUserId())
                .userEmail(req.getUserEmail())
                .type(req.getType())
                .title(req.getTitle())
                .message(req.getMessage())
                .build();

        notificationRepository.save(notification);
        log.info("Notification creee : {} pour {}", req.getType(), req.getUserEmail());

        // 2. Envoyer l email si demande
        if (req.isSendEmail()) {
            emailService.send(req.getUserEmail(), req.getTitle(), req.getMessage());
        }

        // 3. Pousser en temps reel via WebSocket
        // Le client ecoute sur /user/{userId}/queue/notifications
        WebSocketMessage wsMessage = WebSocketMessage.builder()
                .type("NOTIFICATION")
                .userId(req.getUserId().toString())
                .userEmail(req.getUserEmail())
                .content(req.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSendToUser(
                req.getUserId().toString(),
                "/queue/notifications",
                wsMessage
        );

        return toResponse(notification);
    }

    // -----------------------------------------------------------------------
    // MES NOTIFICATIONS
    // -----------------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<NotificationResponse> myNotifications() {
        UUID userId = getAuthenticatedUserId();
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> myUnread() {
        UUID userId = getAuthenticatedUserId();
        return notificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public long countUnread() {
        UUID userId = getAuthenticatedUserId();
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    // -----------------------------------------------------------------------
    // MARQUER COMME LU
    // -----------------------------------------------------------------------
    @Transactional
    public void markAllAsRead() {
        UUID userId = getAuthenticatedUserId();
        notificationRepository.markAllAsReadByUserId(userId);
        log.info("Notifications marquees comme lues pour {}", userId);
    }

    @Transactional
    public NotificationResponse markAsRead(UUID notificationId) {
        UUID userId = getAuthenticatedUserId();

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> BusinessException.notFound("Notification introuvable"));

        if (!notification.getUserId().equals(userId)) {
            throw BusinessException.badRequest("Cette notification ne vous appartient pas");
        }

        notification.setRead(true);
        return toResponse(notification);
    }

    // -----------------------------------------------------------------------
    // UTILITAIRES
    // -----------------------------------------------------------------------
    private UUID getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return UUID.fromString((String) auth.getCredentials());
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType().name())
                .title(n.getTitle())
                .message(n.getMessage())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}