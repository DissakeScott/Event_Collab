package com.eventcollab.notification.service;

import com.eventcollab.notification.domain.*;
import com.eventcollab.notification.dto.*;
import com.eventcollab.notification.repository.NotificationRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock NotificationRepository  notificationRepository;
    @Mock EmailService             emailService;
    @Mock SimpMessagingTemplate    messagingTemplate;
    @InjectMocks NotificationService notificationService;

    private final UUID   userId    = UUID.randomUUID();
    private final String userEmail = "alice@test.com";

    @BeforeEach
    void setupAuth() {
        var auth = new UsernamePasswordAuthenticationToken(
                userEmail, userId.toString(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("send : cree notification + envoie email + pousse WebSocket")
    void send_success() {
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SendNotificationRequest req = new SendNotificationRequest();
        req.setUserId(userId);
        req.setUserEmail(userEmail);
        req.setType(NotificationType.TICKET_BOOKED);
        req.setTitle("Billet reserve");
        req.setMessage("Votre billet est confirme");
        req.setSendEmail(true);

        NotificationResponse result = notificationService.send(req);

        assertThat(result.getType()).isEqualTo("TICKET_BOOKED");
        assertThat(result.getTitle()).isEqualTo("Billet reserve");
        verify(notificationRepository).save(any(Notification.class));
        verify(emailService).send(eq(userEmail), any(), any());
        verify(messagingTemplate).convertAndSendToUser(any(), any(), any());
    }

    @Test
    @DisplayName("send : pas d email si sendEmail = false")
    void send_noEmail() {
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SendNotificationRequest req = new SendNotificationRequest();
        req.setUserId(userId);
        req.setUserEmail(userEmail);
        req.setType(NotificationType.CHAT_MESSAGE);
        req.setTitle("Nouveau message");
        req.setMessage("Hello !");
        req.setSendEmail(false);

        notificationService.send(req);

        verify(emailService, never()).send(any(), any(), any());
        verify(messagingTemplate).convertAndSendToUser(any(), any(), any());
    }

    @Test
    @DisplayName("countUnread : retourne le bon nombre")
    void countUnread() {
        when(notificationRepository.countByUserIdAndReadFalse(userId)).thenReturn(5L);
        assertThat(notificationService.countUnread()).isEqualTo(5L);
    }

    @Test
    @DisplayName("markAllAsRead : appelle le repository")
    void markAllAsRead() {
        notificationService.markAllAsRead();
        verify(notificationRepository).markAllAsReadByUserId(userId);
    }
}