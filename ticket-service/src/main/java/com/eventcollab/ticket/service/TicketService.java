package com.eventcollab.ticket.service;

import com.eventcollab.common.exception.BusinessException;
import com.eventcollab.ticket.client.EventServiceClient;
import com.eventcollab.ticket.client.NotificationServiceClient;
import com.eventcollab.ticket.domain.*;
import com.eventcollab.ticket.dto.*;
import com.eventcollab.ticket.repository.TicketRepository;
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
public class TicketService {

    private final TicketRepository     ticketRepository;
    private final EventServiceClient   eventClient;
    private final QrCodeService        qrCodeService;
    private final NotificationServiceClient notificationClient;
    // -----------------------------------------------------------------------
    // RESERVER un billet
    // -----------------------------------------------------------------------
    @Transactional
    public TicketResponse book(BookTicketRequest req) {
        UUID   userId    = getAuthenticatedUserId();
        String userEmail = getAuthenticatedEmail();

        // 1. Verifier que l evenement existe et est publie
        EventInfo event = eventClient.getEvent(req.getEventId());

        if (!"PUBLISHED".equals(event.getStatus())) {
            throw BusinessException.badRequest(
                    "L evenement n est pas ouvert aux reservations"
            );
        }

        // 2. Protection anti-overbooking
        // On verifie la capacite cote ticket-service AVANT d inserer
        // pour eviter les race conditions
        if (event.isFull()) {
            throw BusinessException.conflict(
                    "L evenement est complet — plus de places disponibles"
            );
        }

        // 3. Un user ne peut pas reserver deux fois le meme evenement
        boolean alreadyBooked = ticketRepository.existsByEventIdAndUserIdAndStatus(
                req.getEventId(), userId, TicketStatus.ACTIVE
        );
        if (alreadyBooked) {
            throw BusinessException.conflict(
                    "Vous avez deja un billet actif pour cet evenement"
            );
        }

        // 4. Creer le ticket (sans QR code d abord pour avoir l ID)
        Ticket ticket = Ticket.builder()
                .eventId(req.getEventId())
                .userId(userId)
                .userEmail(userEmail)
                .build();

        ticketRepository.save(ticket);
        // Incrementer la capacite dans event-service
        eventClient.incrementCapacity(req.getEventId());

        // 5. Generer le QR code avec l ID du ticket
        String qrCode = qrCodeService.generate(ticket.getId(), req.getEventId(), userId);
        ticket.setQrCode(qrCode);

        log.info("Billet reserve : ticket={} event={} user={}",
                ticket.getId(), req.getEventId(), userEmail);

         // --- NOUVEAU : Envoi de la notification ---
    try {
        notificationClient.send(NotificationRequest.builder()
                .userId(userId)
                .userEmail(userEmail)
                .type("TICKET_BOOKED")
                .title("Billet Confirmé !")
                .message("Votre réservation est confirmée. Votre billet n° " + ticket.getId() + " est disponible.")
                .sendEmail(false) // Mets true si ton serveur mail est configuré
                .build());
        log.info("Notification envoyée au service pour le ticket {}", ticket.getId());
    } catch (Exception e) {
        log.error("Erreur lors de l'envoi de la notification : {}", e.getMessage());
    }
    // ------------------------------------------       

        return toResponse(ticket);
    }

    // -----------------------------------------------------------------------
    // ANNULER un billet
    // -----------------------------------------------------------------------
    @Transactional
    public TicketResponse cancel(UUID ticketId) {
        UUID userId = getAuthenticatedUserId();

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> BusinessException.notFound("Billet introuvable : " + ticketId));

        // Seul le proprietaire peut annuler son billet
        if (!ticket.getUserId().equals(userId)) {
            throw BusinessException.badRequest("Ce billet ne vous appartient pas");
        }

        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw BusinessException.badRequest("Ce billet est deja annule");
        }

        if (ticket.getStatus() == TicketStatus.USED) {
            throw BusinessException.badRequest("Impossible d annuler un billet deja utilise");
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        ticket.setCancelledAt(LocalDateTime.now());
        eventClient.decrementCapacity(ticket.getEventId());

        ticket.setQrCode(null); // invalider le QR code

        try {
            notificationClient.send(NotificationRequest.builder()
                    .userId(ticket.getUserId())
                    .userEmail(ticket.getUserEmail())
                    .type("TICKET_CANCELLED")
                    .title("Billet Annulé")
                    .message("L'annulation de votre billet pour l'événement " + ticket.getEventId() + " a bien été prise en compte.")
                    .sendEmail(false)
                    .build());
            log.info("Notification d'annulation envoyée au service pour le ticket {}", ticket.getId());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de la notification d'annulation : {}", e.getMessage());
        }

        log.info("Billet annule : ticket={} user={}", ticketId, userEmail());
        return toResponse(ticket);
    }

    // -----------------------------------------------------------------------
    // MES BILLETS
    // -----------------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<TicketResponse> myTickets() {
        UUID userId = getAuthenticatedUserId();
        return ticketRepository.findByUserIdOrderByBookedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    // -----------------------------------------------------------------------
    // BILLET PAR ID
    // -----------------------------------------------------------------------
    @Transactional(readOnly = true)
    public TicketResponse findById(UUID ticketId) {
        UUID userId = getAuthenticatedUserId();

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> BusinessException.notFound("Billet introuvable : " + ticketId));

        if (!ticket.getUserId().equals(userId)) {
            throw BusinessException.badRequest("Ce billet ne vous appartient pas");
        }

        return toResponse(ticket);
    }

    // -----------------------------------------------------------------------
    // BILLETS D UN EVENT (organisateur)
    // -----------------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<TicketResponse> ticketsByEvent(UUID eventId) {
        return ticketRepository.findByEventIdOrderByBookedAtDesc(eventId)
                .stream().map(this::toResponse).toList();
    }

    // -----------------------------------------------------------------------
    // CHECK-IN (scanner le QR code)
    // -----------------------------------------------------------------------
    @Transactional
    public TicketResponse checkIn(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> BusinessException.notFound("Billet introuvable : " + ticketId));

        if (ticket.getStatus() == TicketStatus.USED) {
            throw BusinessException.conflict("Ce billet a deja ete utilise au check-in");
        }

        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw BusinessException.badRequest("Ce billet est annule");
        }

        ticket.setStatus(TicketStatus.USED);
        log.info("Check-in effectue : ticket={}", ticketId);
        return toResponse(ticket);
    }

    // -----------------------------------------------------------------------
    // UTILITAIRES PRIVES
    // -----------------------------------------------------------------------
    private UUID getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return UUID.fromString((String) auth.getCredentials());
    }

    private String getAuthenticatedEmail() {
        return (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    private String userEmail() {
        return getAuthenticatedEmail();
    }

    private TicketResponse toResponse(Ticket ticket) {

        // Récupérer les infos de l'événement pour avoir le titre
    String title = "Événement inconnu";
    try {
        title = eventClient.getEvent(ticket.getEventId()).getTitle();
    } catch (Exception e) {
        log.warn("Impossible de récupérer le titre pour l'event {}", ticket.getEventId());
    }

        return TicketResponse.builder()
                .id(ticket.getId())
                .eventId(ticket.getEventId())
                .eventTitle(title)
                .userId(ticket.getUserId())
                .userEmail(ticket.getUserEmail())
                .status(ticket.getStatus().name())
                .qrCode(ticket.getQrCode())
                .bookedAt(ticket.getBookedAt())
                .cancelledAt(ticket.getCancelledAt())
                .build();
    }
}