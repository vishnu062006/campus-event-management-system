package com.example.campus_events.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transactions")
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Double amount; // positive = credit, negative = debit

    @Column(nullable = false)
    private String type; // "CREDIT" or "DEBIT"

    @Column(nullable = false)
    private String description;

    @Column(name = "balance_after", nullable = false)
    private Double balanceAfter;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public WalletTransaction() {}

    public WalletTransaction(User user, Double amount, String type, String description, Double balanceAfter) {
        this.user = user;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.balanceAfter = balanceAfter;
        this.createdAt = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public User getUser() { return user; }
    public Double getAmount() { return amount; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public Double getBalanceAfter() { return balanceAfter; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}