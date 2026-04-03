package com.example.campus_events.service;

import com.example.campus_events.model.User;
import com.example.campus_events.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

/**
 * Service handling user registration and login logic
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Register a new user — password is hashed before saving
     */
    public User register(String name, String email, String password, String role) {
        System.out.println("[INFO] Registering user: " + email);

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Hash password before saving
        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(name, email, hashedPassword, role);
        User saved = userRepository.save(user);
        System.out.println("[INFO] User registered successfully: " + email);
        return saved;
    }

    /**
     * Login — compare raw password with hashed password
     */
    public User login(String email, String password) {
        System.out.println("[INFO] Login attempt: " + email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // BCrypt comparison
        if (!passwordEncoder.matches(password, user.getPassword())) {
            System.out.println("[WARN] Invalid password for: " + email);
            throw new IllegalArgumentException("Invalid password");
        }

        System.out.println("[INFO] Login successful: " + email);
        return user;
    }

    /**
     * Get user by ID
     */
    public Optional<User> getUserById(int id) {
        System.out.println("[INFO] Fetching user ID: " + id);
        return userRepository.findById(id);
    }
}