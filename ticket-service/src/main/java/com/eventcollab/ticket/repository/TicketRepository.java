package com.eventcollab.ticket.repository;

import com.eventcollab.ticket.domain.Ticket;
import com.eventcollab.ticket.domain.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    // Ticket d un user pour un event précis
    Optional<Ticket> findByEventIdAndUserId(UUID eventId, UUID userId);

    // Tous les tickets d un user
    List<Ticket> findByUserIdOrderByBookedAtDesc(UUID userId);

    // Tous les tickets d un event (pour l organisateur)
    List<Ticket> findByEventIdOrderByBookedAtDesc(UUID eventId);

    // Compter les tickets actifs d un event (pour verifier la capacite)
    long countByEventIdAndStatus(UUID eventId, TicketStatus status);

    // Verifier si un user a deja un ticket actif pour un event
    boolean existsByEventIdAndUserIdAndStatus(UUID eventId, UUID userId, TicketStatus status);
}