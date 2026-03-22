package com.example.campus_events;

import com.example.campus_events.model.Event;
import com.example.campus_events.model.User;
import com.example.campus_events.repository.EventRepository;
import com.example.campus_events.repository.UserRepository;
import com.example.campus_events.service.EventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EventService
 */
@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EventService eventService;

    private User mockOrganizer() {
        return new User("Admin", "admin@example.com", "pass123", "admin");
    }

    @Test
    void testCreateEventSuccess() {
        User organizer = mockOrganizer();
        when(userRepository.findById(1)).thenReturn(Optional.of(organizer));
        when(eventRepository.save(any(Event.class))).thenAnswer(i -> i.getArgument(0));

        Event event = eventService.createEvent(
                "Tech Fest", "Description",
                LocalDateTime.now().plusDays(7),
                "BMS College", 75, 1
        );

        assertEquals("Tech Fest", event.getTitle());
        assertEquals(75, event.getMaxParticipants());
        assertEquals(organizer, event.getOrganizer());
    }

    @Test
    void testCreateEventEmptyTitle() {
        assertThrows(IllegalArgumentException.class, () ->
                eventService.createEvent("", "Desc",
                        LocalDateTime.now().plusDays(7),
                        "BMS", 75, 1)
        );
    }

    @Test
    void testCreateEventPastDate() {
        assertThrows(IllegalArgumentException.class, () ->
                eventService.createEvent("Tech Fest", "Desc",
                        LocalDateTime.now().minusDays(1), // past date
                        "BMS", 75, 1)
        );
    }

    @Test
    void testCreateEventInvalidCapacity() {
        assertThrows(IllegalArgumentException.class, () ->
                eventService.createEvent("Tech Fest", "Desc",
                        LocalDateTime.now().plusDays(7),
                        "BMS", 0, 1) // zero capacity
        );
    }

    @Test
    void testCreateEventOrganizerNotFound() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                eventService.createEvent("Tech Fest", "Desc",
                        LocalDateTime.now().plusDays(7),
                        "BMS", 75, 99)
        );
    }

    @Test
    void testGetAllEvents() {
        User organizer = mockOrganizer();
        List<Event> mockEvents = List.of(
                new Event("Tech Fest", "Desc", LocalDateTime.now().plusDays(7), "BMS", 75, organizer),
                new Event("Cultural Fest", "Desc", LocalDateTime.now().plusDays(14), "BMS", 100, organizer)
        );
        when(eventRepository.findAll()).thenReturn(mockEvents);

        List<Event> events = eventService.getAllEvents();
        assertEquals(2, events.size());
    }

    @Test
    void testDeleteEventSuccess() {
        when(eventRepository.existsById(1)).thenReturn(true);

        boolean result = eventService.deleteEvent(1);
        assertTrue(result);
        verify(eventRepository, times(1)).deleteById(1);
    }

    @Test
    void testDeleteEventNotFound() {
        when(eventRepository.existsById(99)).thenReturn(false);

        boolean result = eventService.deleteEvent(99);
        assertFalse(result);
    }
}