package com.eventcollab.event.service;

import com.eventcollab.common.exception.BusinessException;
import com.eventcollab.event.domain.*;
import com.eventcollab.event.dto.*;
import com.eventcollab.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class EventService {

    private final EventRepository eventRepository;

    @Transactional
    public EventResponse create(CreateEventRequest req) {
        validateDates(req.getStartDate(), req.getEndDate());

        Authentication auth        = SecurityContextHolder.getContext().getAuthentication();
        UUID           organizerId = UUID.fromString((String) auth.getCredentials());
        String         orgEmail    = (String) auth.getPrincipal();

        Event event = Event.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .location(req.getLocation())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .maxCapacity(req.getMaxCapacity())
                .organizerId(organizerId)
                .organizerEmail(orgEmail)
                .build();

        eventRepository.save(event);
        log.info("Evenement cree : {} par {}", event.getTitle(), orgEmail);
        return toResponse(event);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> findAllPublished() {
        return eventRepository
                .findByStatusOrderByStartDateAsc(EventStatus.PUBLISHED)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public EventResponse findById(UUID id) {
        return eventRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> BusinessException.notFound("Evenement introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public List<EventResponse> findMyEvents() {
        UUID organizerId = getAuthenticatedUserId();
        return eventRepository
                .findByOrganizerIdOrderByStartDateDesc(organizerId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<EventResponse> search(String keyword) {
        return eventRepository.searchByTitle(keyword)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public EventResponse update(UUID id, UpdateEventRequest req) {
        Event event = findEventOwnedBy(id, getAuthenticatedUserId());

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw BusinessException.badRequest("Impossible de modifier un evenement annule");
        }

        if (req.getTitle()       != null) event.setTitle(req.getTitle());
        if (req.getDescription() != null) event.setDescription(req.getDescription());
        if (req.getLocation()    != null) event.setLocation(req.getLocation());
        if (req.getStartDate()   != null) event.setStartDate(req.getStartDate());
        if (req.getEndDate()     != null) event.setEndDate(req.getEndDate());

        if (req.getMaxCapacity() != null) {
            if (req.getMaxCapacity() < event.getCurrentCapacity()) {
                throw BusinessException.badRequest(
                        "La nouvelle capacite (" + req.getMaxCapacity() +
                        ") est inferieure au nombre d inscrits (" + event.getCurrentCapacity() + ")"
                );
            }
            event.setMaxCapacity(req.getMaxCapacity());
        }

        if (req.getStartDate() != null || req.getEndDate() != null) {
            validateDates(event.getStartDate(), event.getEndDate());
        }

        log.info("Evenement mis a jour : {}", id);
        return toResponse(event);
    }

    @Transactional
    public EventResponse publish(UUID id) {
        Event event = findEventOwnedBy(id, getAuthenticatedUserId());

        if (event.getStatus() != EventStatus.DRAFT) {
            throw BusinessException.badRequest("Seul un evenement en DRAFT peut etre publie");
        }
        if (event.getStartDate().isBefore(LocalDateTime.now())) {
            throw BusinessException.badRequest("Impossible de publier un evenement dont la date est passee");
        }

        event.setStatus(EventStatus.PUBLISHED);
        log.info("Evenement publie : {}", id);
        return toResponse(event);
    }

    @Transactional
    public EventResponse cancel(UUID id) {
        Event event = findEventOwnedBy(id, getAuthenticatedUserId());

        if (event.getStatus() == EventStatus.COMPLETED) {
            throw BusinessException.badRequest("Impossible d annuler un evenement termine");
        }
        if (event.getStatus() == EventStatus.CANCELLED) {
            throw BusinessException.badRequest("Evenement deja annule");
        }

        event.setStatus(EventStatus.CANCELLED);
        log.info("Evenement annule : {}", id);
        return toResponse(event);
    }

    @Transactional
    public void delete(UUID id) {
        Event event = findEventOwnedBy(id, getAuthenticatedUserId());

        if (event.getStatus() == EventStatus.PUBLISHED) {
            throw BusinessException.badRequest(
                    "Impossible de supprimer un evenement publie. Annulez-le d abord"
            );
        }

        eventRepository.delete(event);
        log.info("Evenement supprime : {}", id);
    }

    @Transactional
    public EventResponse incrementCapacity(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Evenement introuvable : " + id));
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw BusinessException.badRequest("L evenement n est pas ouvert aux inscriptions");
        }
        event.incrementCapacity();
        return toResponse(event);
    }

    @Transactional
    public EventResponse decrementCapacity(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Evenement introuvable : " + id));
        event.decrementCapacity();
        return toResponse(event);
    }

    private Event findEventOwnedBy(UUID eventId, UUID organizerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> BusinessException.notFound("Evenement introuvable : " + eventId));
        if (!event.getOrganizerId().equals(organizerId)) {
            throw BusinessException.badRequest("Vous n etes pas l organisateur de cet evenement");
        }
        return event;
    }

    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return;
        if (!end.isAfter(start)) {
            throw BusinessException.badRequest("La date de fin doit etre apres la date de debut");
        }
    }

    private UUID getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return UUID.fromString((String) auth.getCredentials());
    }

    private EventResponse toResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .location(event.getLocation())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .maxCapacity(event.getMaxCapacity())
                .currentCapacity(event.getCurrentCapacity())
                .availableSpots(event.availableSpots())
                .isFull(event.isFull())
                .status(event.getStatus().name())
                .organizerId(event.getOrganizerId())
                .organizerEmail(event.getOrganizerEmail())
                .createdAt(event.getCreatedAt())
                .build();
    }
}