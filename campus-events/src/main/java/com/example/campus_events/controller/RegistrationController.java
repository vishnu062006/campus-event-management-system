package com.example.campus_events.controller;

import com.example.campus_events.model.Registration;
import com.example.campus_events.model.WaitingList;
import com.example.campus_events.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class RegistrationController {

    @Autowired private RegistrationService registrationService;

    @PostMapping("/{id}/register")
    public ResponseEntity<?> register(@PathVariable Integer id,
                                      @RequestBody Map<String, Integer> body) {
        try {
            Registration r = registrationService.registerForEvent(body.get("userId"), id);
            return ResponseEntity.ok(Map.of(
                    "message", "Registration successful",
                    "registrationId", r.getId()
            ));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/register")
    public ResponseEntity<?> cancel(@PathVariable Integer id,
                                    @RequestBody Map<String, Integer> body) {
        try {
            boolean cancelled = registrationService.cancelRegistration(body.get("userId"), id);
            return cancelled
                    ? ResponseEntity.ok(Map.of("message", "Registration cancelled"))
                    : ResponseEntity.badRequest().body(Map.of("message", "Registration not found"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<List<Registration>> getParticipants(@PathVariable Integer id) {
        return ResponseEntity.ok(registrationService.getParticipants(id));
    }

    @GetMapping("/{id}/waitlist")
    public ResponseEntity<List<WaitingList>> getWaitlist(@PathVariable Integer id) {
        return ResponseEntity.ok(registrationService.getWaitlist(id));
    }

    // ✅ Clean endpoint to check registration status — used by frontend
    @GetMapping("/{id}/status")
    public ResponseEntity<?> getStatus(@PathVariable Integer id,
                                       @RequestParam Integer userId) {
        boolean registered = registrationService.isRegistered(userId, id);
        return ResponseEntity.ok(Map.of("registered", registered));
    }
}