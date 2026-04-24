package com.eventcollab.event.repository;

import com.eventcollab.event.domain.Event;
import com.eventcollab.event.domain.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

    List<Event> findByOrganizerIdOrderByStartDateDesc(UUID organizerId);

    List<Event> findByStatusOrderByStartDateAsc(EventStatus status);

    boolean existsByIdAndOrganizerId(UUID id, UUID organizerId);

    @Query("SELECT e FROM Event e WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Event> searchByTitle(String keyword);
}