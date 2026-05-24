package com.example.campus_events.repository;

import com.example.campus_events.model.Event;
import com.example.campus_events.model.User;
import com.example.campus_events.model.WaitingList;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WaitingListRepository extends JpaRepository<WaitingList, Integer> {
    boolean existsByUserAndEvent(User user, Event event);
    Optional<WaitingList> findByUserAndEvent(User user, Event event);
    List<WaitingList> findByEventOrderByPositionAsc(Event event);
    long countByEvent(Event event);
}