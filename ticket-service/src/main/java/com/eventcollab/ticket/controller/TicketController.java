package com.eventcollab.ticket.controller;

import com.eventcollab.common.dto.ApiResponse;
import com.eventcollab.ticket.dto.*;
import com.eventcollab.ticket.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    // POST /api/v1/tickets — reserver un billet
    @PostMapping
    public ResponseEntity<ApiResponse<TicketResponse>> book(
            @Valid @RequestBody BookTicketRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Billet reserve avec succes", ticketService.book(req)));
    }

    // GET /api/v1/tickets/me — mes billets
    @GetMapping("/me")
    public ApiResponse<List<TicketResponse>> myTickets() {
        return ApiResponse.ok(ticketService.myTickets());
    }

    // GET /api/v1/tickets/{id} — un billet specifique
    @GetMapping("/{id}")
    public ApiResponse<TicketResponse> getById(@PathVariable UUID id) {
        return ApiResponse.ok(ticketService.findById(id));
    }

    // DELETE /api/v1/tickets/{id} — annuler son billet
    @DeleteMapping("/{id}")
    public ApiResponse<TicketResponse> cancel(@PathVariable UUID id) {
        return ApiResponse.ok("Billet annule", ticketService.cancel(id));
    }

    // GET /api/v1/tickets/event/{eventId} — billets d un event (ORGANIZER)
    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ApiResponse<List<TicketResponse>> byEvent(@PathVariable UUID eventId) {
        return ApiResponse.ok(ticketService.ticketsByEvent(eventId));
    }

    // POST /api/v1/tickets/{id}/checkin — check-in (ORGANIZER)
    @PostMapping("/{id}/checkin")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ApiResponse<TicketResponse> checkIn(@PathVariable UUID id) {
        return ApiResponse.ok("Check-in effectue", ticketService.checkIn(id));
    }
}