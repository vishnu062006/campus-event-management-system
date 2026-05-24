package com.example.campus_events.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    @Column(name = "wallet_balance", nullable = false)
    private Double walletBalance = 1000.0; // default ₹1000 on signup

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public User() { }

    public User(String name, String email, String password, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.createdAt = LocalDateTime.now();
        this.walletBalance = 1000.0;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Double getWalletBalance() { return walletBalance; }
    public void setWalletBalance(Double walletBalance) { this.walletBalance = walletBalance; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}