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
@CrossOrigin(origins = "*")
public class RegistrationController {

    @Autowired private RegistrationService registrationService;

    @PostMapping("/{id}/register")
    public ResponseEntity<?> register(@PathVariable Integer id,
                                      @RequestBody Map<String, Object> body) { // Changed to Object to prevent 400 type crashes
        try {
            if (!body.containsKey("userId") || body.get("userId") == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "userId is required"));
            }

            // Safely extract and convert userId whether frontend sent a String or an Integer
            Integer userId;
            Object userIdObj = body.get("userId");
            if (userIdObj instanceof Integer) {
                userId = (Integer) userIdObj;
            } else {
                userId = Integer.parseInt(userIdObj.toString());
            }

            Registration r = registrationService.registerForEvent(userId, id);

            return ResponseEntity.ok(Map.of(
                    "message", "Registration successful",
                    "registrationId", r.getId()
            ));

        } catch (IllegalStateException e) {
            if (e.getMessage().contains("waitlist")) {
                return ResponseEntity.status(202).body(Map.of(
                        "message", e.getMessage(),
                        "waitlisted", true
                ));
            }
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            // Catch-all for any other backend crashes so you get a clean error message instead of a silent 500
            return ResponseEntity.badRequest().body(Map.of("message", "Server error: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/register")
    public ResponseEntity<?> cancel(@PathVariable Integer id,
                                    @RequestBody Map<String, Object> body) {
        try {
            Integer userId = Integer.parseInt(body.get("userId").toString());
            boolean cancelled = registrationService.cancelRegistration(userId, id);
            return cancelled
                    ? ResponseEntity.ok(Map.of("message", "Registration cancelled"))
                    : ResponseEntity.badRequest().body(Map.of("message", "Registration not found"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/waitlist")
    public ResponseEntity<?> leaveWaitlist(@PathVariable Integer id,
                                           @RequestBody Map<String, Object> body) {
        try {
            Integer userId = Integer.parseInt(body.get("userId").toString());
            registrationService.leaveWaitlist(userId, id);
            return ResponseEntity.ok(Map.of("message", "Removed from waitlist"));
        } catch (Exception e) {
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

    @GetMapping("/{id}/status")
    public ResponseEntity<?> getStatus(@PathVariable Integer id,
                                       @RequestParam Integer userId) {
        boolean registered = registrationService.isRegistered(userId, id);
        Integer waitlistPos = registrationService.getWaitlistPosition(userId, id).orElse(null);
        return ResponseEntity.ok(Map.of(
                "registered", registered,
                "waitlistPosition", waitlistPos != null ? waitlistPos : 0
        ));
    }
}