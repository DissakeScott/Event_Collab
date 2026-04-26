package com.eventcollab.ticket.service;

import com.eventcollab.common.exception.BusinessException;
import com.eventcollab.ticket.client.EventServiceClient;
import com.eventcollab.ticket.domain.*;
import com.eventcollab.ticket.dto.*;
import com.eventcollab.ticket.repository.TicketRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock TicketRepository   ticketRepository;
    @Mock EventServiceClient eventClient;
    @Mock QrCodeService      qrCodeService;
    @InjectMocks TicketService ticketService;

    private final UUID   userId    = UUID.randomUUID();
    private final UUID   eventId   = UUID.randomUUID();
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
    @DisplayName("book : succes pour un evenement disponible")
    void book_success() {
        EventInfo event = buildEventInfo(false, "PUBLISHED");
        when(eventClient.getEvent(eventId)).thenReturn(event);
        when(ticketRepository.existsByEventIdAndUserIdAndStatus(any(), any(), any())).thenReturn(false);
        when(ticketRepository.save(any())).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t = Ticket.builder()
                    .id(UUID.randomUUID())
                    .eventId(t.getEventId())
                    .userId(t.getUserId())
                    .userEmail(t.getUserEmail())
                    .status(TicketStatus.ACTIVE)
                    .build();
            return t;
        });
        when(qrCodeService.generate(any(), any(), any())).thenReturn("data:image/png;base64,xxx");

        BookTicketRequest req = new BookTicketRequest();
        req.setEventId(eventId);

        TicketResponse result = ticketService.book(req);

        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        assertThat(result.getUserEmail()).isEqualTo(userEmail);
    }

    @Test
    @DisplayName("book : echoue si evenement complet")
    void book_eventFull() {
        EventInfo event = buildEventInfo(true, "PUBLISHED");
        when(eventClient.getEvent(eventId)).thenReturn(event);

        BookTicketRequest req = new BookTicketRequest();
        req.setEventId(eventId);

        assertThatThrownBy(() -> ticketService.book(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("complet");
    }

    @Test
    @DisplayName("book : echoue si deja un billet actif")
    void book_alreadyBooked() {
        EventInfo event = buildEventInfo(false, "PUBLISHED");
        when(eventClient.getEvent(eventId)).thenReturn(event);
        when(ticketRepository.existsByEventIdAndUserIdAndStatus(any(), any(), any())).thenReturn(true);

        BookTicketRequest req = new BookTicketRequest();
        req.setEventId(eventId);

        assertThatThrownBy(() -> ticketService.book(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("deja un billet");
    }

    @Test
    @DisplayName("book : echoue si evenement non publie")
    void book_eventNotPublished() {
        EventInfo event = buildEventInfo(false, "DRAFT");
        when(eventClient.getEvent(eventId)).thenReturn(event);

        BookTicketRequest req = new BookTicketRequest();
        req.setEventId(eventId);

        assertThatThrownBy(() -> ticketService.book(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("pas ouvert");
    }

    @Test
    @DisplayName("cancel : succes pour un billet actif")
    void cancel_success() {
        Ticket ticket = buildTicket(TicketStatus.ACTIVE);
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        TicketResponse result = ticketService.cancel(ticket.getId());

        assertThat(result.getStatus()).isEqualTo("CANCELLED");
        assertThat(result.getCancelledAt()).isNotNull();
    }

    @Test
    @DisplayName("cancel : echoue si billet deja annule")
    void cancel_alreadyCancelled() {
        Ticket ticket = buildTicket(TicketStatus.CANCELLED);
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.cancel(ticket.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("deja annule");
    }

    private EventInfo buildEventInfo(boolean isFull, String status) {
        EventInfo e = new EventInfo();
        e.setId(eventId);
        e.setStatus(status);
        e.setFull(isFull);
        e.setMaxCapacity(100);
        e.setCurrentCapacity(isFull ? 100 : 50);
        return e;
    }

    private Ticket buildTicket(TicketStatus status) {
        return Ticket.builder()
                .id(UUID.randomUUID())
                .eventId(eventId)
                .userId(userId)
                .userEmail(userEmail)
                .status(status)
                .build();
    }
}