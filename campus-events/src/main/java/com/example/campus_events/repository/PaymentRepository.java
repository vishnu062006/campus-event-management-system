package com.example.campus_events.repository;

import com.example.campus_events.model.Event;
import com.example.campus_events.model.Payment;
import com.example.campus_events.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByUserOrderByCreatedAtDesc(User user);
    List<Payment> findByEventOrderByCreatedAtDesc(Event event);
    Optional<Payment> findByUserAndEventAndType(User user, Event event, String type);
}