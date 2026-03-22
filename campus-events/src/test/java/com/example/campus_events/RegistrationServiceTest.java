package com.example.campus_events;

import com.example.campus_events.model.Event;
import com.example.campus_events.model.Registration;
import com.example.campus_events.model.User;
import com.example.campus_events.repository.EventRepository;
import com.example.campus_events.repository.RegistrationRepository;
import com.example.campus_events.repository.UserRepository;
import com.example.campus_events.service.RegistrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RegistrationService
 * Tests capacity validation and duplicate registration
 */
@ExtendWith(MockitoExtension.class)
public class RegistrationServiceTest {

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RegistrationService registrationService;

    // Helper methods
    private User mockUser() {
        return new User("Vishnu", "vishnu@example.com", "pass123", "student");
    }

    private Event mockEvent(int maxParticipants) {
        return new Event("Tech Fest", "Description",
                LocalDateTime.now().plusDays(7),
                "BMS College", maxParticipants,
                new User("Admin", "admin@example.com", "pass123", "admin"));
    }

    @Test
    void testRegisterSuccess() {
        User user = mockUser();
        Event event = mockEvent(75);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
        when(registrationRepository.existsByUserAndEvent(user, event)).thenReturn(false);
        when(registrationRepository.countByEvent(event)).thenReturn(10L);
        when(registrationRepository.save(any(Registration.class))).thenAnswer(i -> i.getArgument(0));

        Registration reg = registrationService.registerForEvent(1, 1);

        assertNotNull(reg);
        assertEquals(user, reg.getUser());
        assertEquals(event, reg.getEvent());
    }

    @Test
    void testCapacityEnforced() {
        User user = mockUser();
        Event event = mockEvent(75);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
        when(registrationRepository.existsByUserAndEvent(user, event)).thenReturn(false);
        when(registrationRepository.countByEvent(event)).thenReturn(75L); // already full!

        assertThrows(IllegalStateException.class, () ->
                registrationService.registerForEvent(1, 1)
        );
    }

    @Test
    void testCapacityAt74() {
        User user = mockUser();
        Event event = mockEvent(75);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
        when(registrationRepository.existsByUserAndEvent(user, event)).thenReturn(false);
        when(registrationRepository.countByEvent(event)).thenReturn(74L); // one spot left
        when(registrationRepository.save(any(Registration.class))).thenAnswer(i -> i.getArgument(0));

        Registration reg = registrationService.registerForEvent(1, 1);
        assertNotNull(reg); // should succeed
    }

    @Test
    void testDuplicateRegistration() {
        User user = mockUser();
        Event event = mockEvent(75);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
        when(registrationRepository.existsByUserAndEvent(user, event)).thenReturn(true); // already registered!

        assertThrows(IllegalStateException.class, () ->
                registrationService.registerForEvent(1, 1)
        );
    }

    @Test
    void testUserNotFound() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                registrationService.registerForEvent(99, 1)
        );
    }

    @Test
    void testEventNotFound() {
        User user = mockUser();
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(eventRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                registrationService.registerForEvent(1, 99)
        );
    }

    @Test
    void testCancelRegistration() {
        User user = mockUser();
        Event event = mockEvent(75);
        Registration registration = new Registration(user, event);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
        when(registrationRepository.findByUserAndEvent(user, event)).thenReturn(Optional.of(registration));

        boolean result = registrationService.cancelRegistration(1, 1);
        assertTrue(result);
    }
}