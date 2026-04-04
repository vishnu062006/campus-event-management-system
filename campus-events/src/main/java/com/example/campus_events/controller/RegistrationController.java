package com.example.campus_events.controller;

import com.example.campus_events.model.Registration;
import com.example.campus_events.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Registration endpoints
 */
@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    /**
     * POST /api/events/{id}/register
     */
    @PostMapping("/{id}/register")
    public ResponseEntity<?> register(@PathVariable int id,
                                      @RequestBody Map<String, Integer> body) {
        Integer userId = body.get("userId");
        Registration registration = registrationService.registerForEvent(userId, id);
        return ResponseEntity.status(201).body(registration);
    }

    /**
     * DELETE /api/events/{id}/register
     */
    @DeleteMapping("/{id}/register")
    public ResponseEntity<?> cancelRegistration(@PathVariable int id,
                                                @RequestBody Map<String, Integer> body) {
        Integer userId = body.get("userId");
        boolean cancelled = registrationService.cancelRegistration(userId, id);
        if (cancelled) return ResponseEntity.ok(Map.of("message", "Registration cancelled"));
        return ResponseEntity.notFound().build();
    }

    /**
     * GET /api/events/{id}/participants
     */
    @GetMapping("/{id}/participants")
    public ResponseEntity<?> getParticipants(@PathVariable int id) {
        List<Registration> participants = registrationService.getParticipants(id);
        return ResponseEntity.ok(participants);
    }
}