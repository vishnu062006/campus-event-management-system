package com.example.campus_events.service;

import com.example.campus_events.model.Event;
import com.example.campus_events.model.EventStatus;
import com.example.campus_events.model.User;
import com.example.campus_events.repository.EventRepository;
import com.example.campus_events.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service handling event creation and management
 */
@Service
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Auto calculate event status based on current time
     */
    private EventStatus calculateStatus(LocalDateTime eventDate, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();
        if (endTime != null && now.isAfter(endTime)) return EventStatus.EXPIRED;
        if (now.isAfter(eventDate)) return EventStatus.ONGOING;
        return EventStatus.UPCOMING;
    }

    /**
     * Create a new event
     */
    public Event createEvent(String title, String description, LocalDateTime eventDate,
                             LocalDateTime startTime, LocalDateTime endTime,
                             String location, Integer maxParticipants,
                             Double entryFee, Integer organizerId) {
        log.info("Creating event: {}", title);

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (eventDate == null || eventDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Event date must be in the future");
        }
        if (maxParticipants == null || maxParticipants <= 0) {
            throw new IllegalArgumentException("Max participants must be positive");
        }

        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new IllegalArgumentException("Organizer not found"));

        Event event = new Event(title, description, eventDate, location, maxParticipants, organizer);
        event.setStartTime(startTime);
        event.setEndTime(endTime);
        event.setEntryFee(entryFee != null ? entryFee : 0.0);
        event.setStatus(calculateStatus(eventDate, endTime));

        Event saved = eventRepository.save(event);
        log.info("Event created successfully: {}", title);
        return saved;
    }

    /**
     * Get all events with auto-updated status
     */
    public List<Event> getAllEvents() {
        log.info("Fetching all events");
        List<Event> events = eventRepository.findAll();
        events.forEach(e -> {
            EventStatus newStatus = calculateStatus(e.getEventDate(), e.getEndTime());
            if (e.getStatus() != newStatus) {
                e.setStatus(newStatus);
                eventRepository.save(e);
            }
        });
        return events;
    }

    /**
     * Get events by status
     */
    public List<Event> getEventsByStatus(EventStatus status) {
        log.info("Fetching events by status: {}", status);
        return eventRepository.findByStatus(status);
    }

    /**
     * Get event by ID
     */
    public Optional<Event> getEventById(int id) {
        log.info("Fetching event ID: {}", id);
        return eventRepository.findById(id);
    }

    /**
     * Update event
     */
    public Optional<Event> updateEvent(int id, String title, String description,
                                       LocalDateTime eventDate, LocalDateTime startTime,
                                       LocalDateTime endTime, String location,
                                       Integer maxParticipants, Double entryFee) {
        log.info("Updating event ID: {}", id);
        Optional<Event> found = eventRepository.findById(id);
        found.ifPresent(e -> {
            e.setTitle(title);
            e.setDescription(description);
            e.setEventDate(eventDate);
            e.setStartTime(startTime);
            e.setEndTime(endTime);
            e.setLocation(location);
            e.setMaxParticipants(maxParticipants);
            e.setEntryFee(entryFee != null ? entryFee : 0.0);
            e.setStatus(calculateStatus(eventDate, endTime));
            eventRepository.save(e);
            log.info("Event updated: {}", id);
        });
        return found;
    }

    /**
     * Delete event
     */
    public boolean deleteEvent(int id) {
        if (eventRepository.existsById(id)) {
            eventRepository.deleteById(id);
            log.info("Event deleted: {}", id);
            return true;
        }
        log.warn("Event not found: {}", id);
        return false;
    }
}