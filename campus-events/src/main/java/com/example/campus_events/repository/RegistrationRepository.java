package com.example.campus_events.repository;

import com.example.campus_events.model.Event;
import com.example.campus_events.model.Registration;
import com.example.campus_events.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Integer> {

    // Check if user already registered for event
    boolean existsByUserAndEvent(User user, Event event);

    // Count registrations for an event
    long countByEvent(Event event);

    // Get all registrations for an event
    List<Registration> findByEvent(Event event);

    // Get all registrations for a user
    List<Registration> findByUser(User user);

    // Find specific registration
    Optional<Registration> findByUserAndEvent(User user, Event event);
}