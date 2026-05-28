package com.example.campus_events.controller;

import com.example.campus_events.model.AccessRequest;
import com.example.campus_events.service.AccessRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/access-requests")
public class AccessRequestController {

    @Autowired
    private AccessRequestService accessRequestService;

    @PostMapping
    public ResponseEntity<?> requestAccess(Authentication authentication) {
        try {
            return ResponseEntity.ok(accessRequestService.createRequest(authentication.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/my-status")
    public ResponseEntity<?> getMyStatus(Authentication authentication) {
        return accessRequestService.getMyStatus(authentication.getName())
                .map(req -> ResponseEntity.ok(Map.of("status", req.getStatus())))
                .orElse(ResponseEntity.ok(Map.of("status", "")));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllRequests() {
        return ResponseEntity.ok(accessRequestService.getAllRequests());
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approve(@PathVariable UUID id) {
        try {
            accessRequestService.approve(id);
            return ResponseEntity.ok(Map.of("message", "Approved"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> reject(@PathVariable UUID id) {
        try {
            accessRequestService.reject(id);
            return ResponseEntity.ok(Map.of("message", "Rejected"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}