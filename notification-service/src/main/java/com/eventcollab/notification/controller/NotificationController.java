package com.eventcollab.notification.controller;

import com.eventcollab.common.dto.ApiResponse;
import com.eventcollab.notification.dto.*;
import com.eventcollab.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // GET /api/v1/notifications — mes notifications
    @GetMapping
    public ApiResponse<List<NotificationResponse>> myNotifications() {
        return ApiResponse.ok(notificationService.myNotifications());
    }

    // GET /api/v1/notifications/unread — non lues
    @GetMapping("/unread")
    public ApiResponse<List<NotificationResponse>> unread() {
        return ApiResponse.ok(notificationService.myUnread());
    }

    // GET /api/v1/notifications/unread/count — compteur
    @GetMapping("/unread/count")
    public ApiResponse<Long> countUnread() {
        return ApiResponse.ok(notificationService.countUnread());
    }

    // PATCH /api/v1/notifications/read-all
    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ApiResponse.ok("Toutes les notifications marquees comme lues");
    }

    // PATCH /api/v1/notifications/{id}/read
    @PatchMapping("/{id}/read")
    public ApiResponse<NotificationResponse> markAsRead(@PathVariable UUID id) {
        return ApiResponse.ok(notificationService.markAsRead(id));
    }

    // POST /api/v1/notifications/send — envoyer une notif (inter-service)
    @PostMapping("/send")
    
    public ResponseEntity<ApiResponse<NotificationResponse>> send(
            @Valid @RequestBody SendNotificationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Notification envoyee", notificationService.send(req)));
    }
}