package com.example.campus_events.repository;

import com.example.campus_events.model.Event;
import com.example.campus_events.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for Event entity
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findByOrganizer(User organizer);
}