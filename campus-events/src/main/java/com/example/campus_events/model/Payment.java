package com.example.campus_events.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private Double amount;

    @Column(name = "platform_fee")
    private Double platformFee = 0.0;

    @Column(nullable = false)
    private String type; // "PAYMENT" or "REFUND"

    @Column(nullable = false)
    private String status; // "SUCCESS"

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Payment() {}

    public Payment(User user, Event event, Double amount, Double platformFee, String type) {
        this.user = user;
        this.event = event;
        this.amount = amount;
        this.platformFee = platformFee;
        this.type = type;
        this.status = "SUCCESS";
        this.createdAt = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public User getUser() { return user; }
    public Event getEvent() { return event; }
    public Double getAmount() { return amount; }
    public Double getPlatformFee() { return platformFee; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}