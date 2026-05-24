package com.example.campus_events.repository;

import com.example.campus_events.model.AccessRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccessRequestRepository extends JpaRepository<AccessRequest, UUID> {
    Optional<AccessRequest> findByUserId(Integer userId);
    List<AccessRequest> findByStatus(String status);
}