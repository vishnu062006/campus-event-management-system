package com.example.campus_events.repository;

import com.example.campus_events.model.User;
import com.example.campus_events.model.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Integer> {
    List<WalletTransaction> findByUserOrderByCreatedAtDesc(User user);
}