package com.example.campus_events.service;

import com.example.campus_events.model.Event;
import com.example.campus_events.model.Registration;
import com.example.campus_events.model.User;
import com.example.campus_events.repository.EventRepository;
import com.example.campus_events.repository.RegistrationRepository;
import com.example.campus_events.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Service handling event registration logic
 * Enforces capacity limits and prevents duplicate registrations
 */
@Service
public class RegistrationService {

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Register a user for an event
     * Checks: duplicate registration, event capacity
     */
    public Registration registerForEvent(Integer userId, Integer eventId) {
        System.out.println("[INFO] Registration attempt - User: " + userId + " Event: " + eventId);

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Find event
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        // Check duplicate registration
        if (registrationRepository.existsByUserAndEvent(user, event)) {
            System.out.println("[WARN] Duplicate registration attempt - User: " + userId);
            throw new IllegalStateException("User already registered for this event");
        }

        // Check capacity
        long currentCount = registrationRepository.countByEvent(event);
        System.out.println("[INFO] Current registrations: " + currentCount + "/" + event.getMaxParticipants());

        if (currentCount >= event.getMaxParticipants()) {
            System.out.println("[WARN] Event full - Event: " + eventId);
            throw new IllegalStateException("Event is full. Maximum capacity reached");
        }

        // Register
        Registration registration = new Registration(user, event);
        Registration saved = registrationRepository.save(registration);
        System.out.println("[INFO] Registration successful - User: " + userId + " Event: " + eventId);
        return saved;
    }

    /**
     * Cancel registration
     */
    public boolean cancelRegistration(Integer userId, Integer eventId) {
        System.out.println("[INFO] Cancellation attempt - User: " + userId + " Event: " + eventId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        return registrationRepository.findByUserAndEvent(user, event)
                .map(r -> {
                    registrationRepository.delete(r);
                    System.out.println("[INFO] Registration cancelled - User: " + userId);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Get all participants for an event
     */
    public List<Registration> getParticipants(Integer eventId) {
        System.out.println("[INFO] Fetching participants for event: " + eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        return registrationRepository.findByEvent(event);
    }
}