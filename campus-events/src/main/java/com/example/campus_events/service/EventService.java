package com.example.campus_events.service;

import com.example.campus_events.model.Event;
import com.example.campus_events.model.User;
import com.example.campus_events.repository.EventRepository;
import com.example.campus_events.repository.UserRepository;
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

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new event
     */
    public Event createEvent(String title, String description, LocalDateTime eventDate,
                             String location, Integer maxParticipants, Integer organizerId) {
        System.out.println("[INFO] Creating event: " + title);

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
        Event saved = eventRepository.save(event);
        System.out.println("[INFO] Event created successfully: " + title);
        return saved;
    }

    /**
     * Get all events
     */
    public List<Event> getAllEvents() {
        System.out.println("[INFO] Fetching all events");
        return eventRepository.findAll();
    }

    /**
     * Get event by ID
     */
    public Optional<Event> getEventById(int id) {
        System.out.println("[INFO] Fetching event ID: " + id);
        return eventRepository.findById(id);
    }

    /**
     * Update event
     */
    public Optional<Event> updateEvent(int id, String title, String description,
                                       LocalDateTime eventDate, String location,
                                       Integer maxParticipants) {
        System.out.println("[INFO] Updating event ID: " + id);
        Optional<Event> found = eventRepository.findById(id);
        found.ifPresent(e -> {
            e.setTitle(title);
            e.setDescription(description);
            e.setEventDate(eventDate);
            e.setLocation(location);
            e.setMaxParticipants(maxParticipants);
            eventRepository.save(e);
            System.out.println("[INFO] Event updated: " + id);
        });
        return found;
    }
    /**
     * Delete event
     */
    public boolean deleteEvent(int id) {
        if (eventRepository.existsById(id)) {
            eventRepository.deleteById(id);
            System.out.println("[INFO] Event deleted: " + id);
            return true;
        }
        System.out.println("[WARN] Event not found: " + id);
        return false;
    }
}