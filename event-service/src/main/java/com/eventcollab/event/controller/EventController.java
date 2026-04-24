package com.eventcollab.event.controller;

import com.eventcollab.common.dto.ApiResponse;
import com.eventcollab.event.dto.*;
import com.eventcollab.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ApiResponse<List<EventResponse>> listPublished() {
        return ApiResponse.ok(eventService.findAllPublished());
    }

    @GetMapping("/search")
    public ApiResponse<List<EventResponse>> search(@RequestParam String keyword) {
        return ApiResponse.ok(eventService.search(keyword));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ApiResponse<List<EventResponse>> myEvents() {
        return ApiResponse.ok(eventService.findMyEvents());
    }

    @GetMapping("/{id}")
    public ApiResponse<EventResponse> getById(@PathVariable UUID id) {
        return ApiResponse.ok(eventService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EventResponse>> create(
            @Valid @RequestBody CreateEventRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Evenement cree avec succes", eventService.create(req)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ApiResponse<EventResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEventRequest req) {
        return ApiResponse.ok("Evenement mis a jour", eventService.update(id, req));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ApiResponse<EventResponse> publish(@PathVariable UUID id) {
        return ApiResponse.ok("Evenement publie", eventService.publish(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ApiResponse<EventResponse> cancel(@PathVariable UUID id) {
        return ApiResponse.ok("Evenement annule", eventService.cancel(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}