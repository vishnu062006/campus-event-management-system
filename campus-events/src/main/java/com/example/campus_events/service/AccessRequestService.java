package com.example.campus_events.service;

import com.example.campus_events.model.AccessRequest;
import com.example.campus_events.model.User;
import com.example.campus_events.repository.AccessRequestRepository;
import com.example.campus_events.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccessRequestService {

    @Autowired
    private AccessRequestRepository accessRequestRepo;

    @Autowired
    private UserRepository userRepo;

    public AccessRequest createRequest(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (accessRequestRepo.findByUserId(user.getId()).isPresent()) {
            throw new RuntimeException("Request already exists");
        }

        AccessRequest req = new AccessRequest();
        req.setUserId(user.getId());
        req.setUserName(user.getName());
        req.setStatus("PENDING");
        req.setRequestedAt(LocalDateTime.now());
        return accessRequestRepo.save(req);
    }

    public Optional<AccessRequest> getMyStatus(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return accessRequestRepo.findByUserId(user.getId());
    }

    public List<AccessRequest> getAllRequests() {
        return accessRequestRepo.findAll();
    }

    public void approve(UUID requestId) {
        AccessRequest req = accessRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        req.setStatus("APPROVED");
        req.setReviewedAt(LocalDateTime.now());
        accessRequestRepo.save(req);

        User user = userRepo.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole("EVENT_ADMIN");
        userRepo.save(user);
    }

    public void reject(UUID requestId) {
        AccessRequest req = accessRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        req.setStatus("REJECTED");
        req.setReviewedAt(LocalDateTime.now());
        accessRequestRepo.save(req);
    }
}