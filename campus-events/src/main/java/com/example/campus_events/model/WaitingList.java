package com.example.campus_events.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "waiting_list",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "event_id"}))
public class WaitingList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(nullable = false)
    private Integer position;

    public WaitingList() { }

    public WaitingList(User user, Event event, Integer position) {
        this.user = user;
        this.event = event;
        this.position = position;
        this.joinedAt = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
}