package com.eventcollab.event.service;

import com.eventcollab.common.exception.BusinessException;
import com.eventcollab.event.domain.*;
import com.eventcollab.event.dto.*;
import com.eventcollab.event.repository.EventRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock EventRepository eventRepository;
    @InjectMocks EventService eventService;

    private final UUID   organizerId    = UUID.randomUUID();
    private final String organizerEmail = "orga@test.com";

    @BeforeEach
    void setupAuth() {
        var auth = new UsernamePasswordAuthenticationToken(
                organizerEmail,
                organizerId.toString(),
                List.of(new SimpleGrantedAuthority("ROLE_ORGANIZER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("create : succes avec dates valides")
    void create_success() {
        when(eventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        EventResponse result = eventService.create(buildCreateRequest());
        assertThat(result.getTitle()).isEqualTo("Concert Jazz");
        assertThat(result.getStatus()).isEqualTo("DRAFT");
        assertThat(result.getOrganizerEmail()).isEqualTo(organizerEmail);
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    @DisplayName("create : echoue si date fin avant date debut")
    void create_invalidDates() {
        CreateEventRequest req = buildCreateRequest();
        req.setStartDate(LocalDateTime.now().plusDays(5));
        req.setEndDate(LocalDateTime.now().plusDays(1));
        assertThatThrownBy(() -> eventService.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("date de fin");
    }

    @Test
    @DisplayName("publish : succes depuis DRAFT")
    void publish_success() {
        Event event = buildEvent(EventStatus.DRAFT);
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        EventResponse result = eventService.publish(event.getId());
        assertThat(result.getStatus()).isEqualTo("PUBLISHED");
    }

    @Test
    @DisplayName("publish : echoue si deja PUBLISHED")
    void publish_alreadyPublished() {
        Event event = buildEvent(EventStatus.PUBLISHED);
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        assertThatThrownBy(() -> eventService.publish(event.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("DRAFT");
    }

    @Test
    @DisplayName("cancel : succes depuis PUBLISHED")
    void cancel_success() {
        Event event = buildEvent(EventStatus.PUBLISHED);
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        assertThat(eventService.cancel(event.getId()).getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    @DisplayName("update : refuse si capacite inferieure aux inscrits")
    void update_capacityBelowRegistrations() {
        Event event = buildEvent(EventStatus.PUBLISHED);
        event.setCurrentCapacity(50);
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        UpdateEventRequest req = new UpdateEventRequest();
        req.setMaxCapacity(30);
        assertThatThrownBy(() -> eventService.update(event.getId(), req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("inferieure au nombre d inscrits");
    }

    private CreateEventRequest buildCreateRequest() {
        CreateEventRequest req = new CreateEventRequest();
        req.setTitle("Concert Jazz");
        req.setLocation("Paris");
        req.setStartDate(LocalDateTime.now().plusDays(10));
        req.setEndDate(LocalDateTime.now().plusDays(10).plusHours(3));
        req.setMaxCapacity(100);
        return req;
    }

    private Event buildEvent(EventStatus status) {
        return Event.builder()
                .id(UUID.randomUUID())
                .title("Concert Jazz")
                .location("Paris")
                .startDate(LocalDateTime.now().plusDays(10))
                .endDate(LocalDateTime.now().plusDays(10).plusHours(3))
                .maxCapacity(100)
                .organizerId(organizerId)
                .organizerEmail(organizerEmail)
                .status(status)
                .build();
    }
}