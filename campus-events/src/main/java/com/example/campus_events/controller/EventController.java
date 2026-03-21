package com.example.campus_events.controller;

import com.example.campus_events.model.Event;
import com.example.campus_events.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Event endpoints
 */
@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {

    @Autowired
    private EventService eventService;

    /**
     * POST /api/events
     */
    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody Map<String, String> body) {
        try {
            String title = body.get("title");
            String description = body.get("description");
            LocalDateTime eventDate = LocalDateTime.parse(body.get("eventDate"));
            String location = body.get("location");
            Integer maxParticipants = Integer.parseInt(body.get("maxParticipants"));
            Integer organizerId = Integer.parseInt(body.get("organizerId"));
            Event event = eventService.createEvent(title, description, eventDate,
                    location, maxParticipants, organizerId);
            return ResponseEntity.status(201).body(event);
        } catch (IllegalArgumentException e) {
            System.out.println("[ERROR] Event creation failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/events
     */
    @GetMapping
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    /**
     * GET /api/events/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getEventById(@PathVariable int id) {
        return eventService.getEventById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/events/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable int id,
                                         @RequestBody Map<String, String> body) {
        try {
            String title = body.get("title");
            String description = body.get("description");
            LocalDateTime eventDate = LocalDateTime.parse(body.get("eventDate"));
            String location = body.get("location");
            Integer maxParticipants = Integer.parseInt(body.get("maxParticipants"));
            return eventService.updateEvent(id, title, description, eventDate,
                            location, maxParticipants)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/events/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable int id) {
        boolean deleted = eventService.deleteEvent(id);
        if (deleted) return ResponseEntity.ok(Map.of("message", "Event deleted"));
        return ResponseEntity.notFound().build();
    }
}