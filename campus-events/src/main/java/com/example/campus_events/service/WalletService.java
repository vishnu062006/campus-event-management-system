package com.example.campus_events.service;

import com.example.campus_events.model.User;
import com.example.campus_events.model.WalletTransaction;
import com.example.campus_events.repository.UserRepository;
import com.example.campus_events.repository.WalletTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WalletService {

    @Autowired private UserRepository userRepository;
    @Autowired private WalletTransactionRepository walletTransactionRepository;

    @Transactional
    public void debit(User user, Double amount, String description) {
        if (user.getWalletBalance() < amount) {
            throw new IllegalStateException(
                    "Insufficient wallet balance. Required: ₹" + amount +
                            ", Available: ₹" + String.format("%.2f", user.getWalletBalance())
            );
        }
        user.setWalletBalance(user.getWalletBalance() - amount);
        userRepository.save(user);
        walletTransactionRepository.save(
                new WalletTransaction(user, -amount, "DEBIT", description, user.getWalletBalance())
        );
    }

    @Transactional
    public void credit(User user, Double amount, String description) {
        user.setWalletBalance(user.getWalletBalance() + amount);
        userRepository.save(user);
        walletTransactionRepository.save(
                new WalletTransaction(user, amount, "CREDIT", description, user.getWalletBalance())
        );
    }

    public List<WalletTransaction> getTransactionHistory(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return walletTransactionRepository.findByUserOrderByCreatedAtDesc(user);
    }
}