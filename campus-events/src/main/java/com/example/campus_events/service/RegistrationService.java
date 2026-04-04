package com.example.campus_events.service;

import com.example.campus_events.model.Event;
import com.example.campus_events.model.Registration;
import com.example.campus_events.model.User;
import com.example.campus_events.repository.EventRepository;
import com.example.campus_events.repository.RegistrationRepository;
import com.example.campus_events.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Service handling event registration logic
 */
@Service
public class RegistrationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Register a user for an event
     */
    public Registration registerForEvent(Integer userId, Integer eventId) {
        log.info("Registration attempt - User: {} Event: {}", userId, eventId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (registrationRepository.existsByUserAndEvent(user, event)) {
            log.warn("Duplicate registration - User: {} Event: {}", userId, eventId);
            throw new IllegalStateException("User already registered for this event");
        }

        long currentCount = registrationRepository.countByEvent(event);
        log.info("Current registrations: {}/{}", currentCount, event.getMaxParticipants());

        if (currentCount >= event.getMaxParticipants()) {
            log.warn("Event full - Event: {}", eventId);
            throw new IllegalStateException("Event is full. Maximum capacity reached");
        }

        Registration registration = new Registration(user, event);
        Registration saved = registrationRepository.save(registration);
        log.info("Registration successful - User: {} Event: {}", userId, eventId);
        return saved;
    }

    /**
     * Cancel registration
     */
    public boolean cancelRegistration(Integer userId, Integer eventId) {
        log.info("Cancellation attempt - User: {} Event: {}", userId, eventId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        return registrationRepository.findByUserAndEvent(user, event)
                .map(r -> {
                    registrationRepository.delete(r);
                    log.info("Registration cancelled - User: {} Event: {}", userId, eventId);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Get all participants for an event
     */
    public List<Registration> getParticipants(Integer eventId) {
        log.info("Fetching participants for event: {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        return registrationRepository.findByEvent(event);
    }
}