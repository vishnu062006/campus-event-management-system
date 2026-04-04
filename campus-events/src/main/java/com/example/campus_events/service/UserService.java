package com.example.campus_events.service;

import com.example.campus_events.model.User;
import com.example.campus_events.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

/**
 * Service handling user registration and login logic
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Register a new user — password is hashed before saving
     */
    public User register(String name, String email, String password, String role) {
        log.info("Registering user: {}", email);

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

        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(name, email, hashedPassword, role);
        User saved = userRepository.save(user);
        log.info("User registered successfully: {}", email);
        return saved;
    }

    /**
     * Login — compare raw password with hashed password
     */
    public User login(String email, String password) {
        log.info("Login attempt: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Invalid password for: {}", email);
            throw new IllegalArgumentException("Invalid password");
        }

        log.info("Login successful: {}", email);
        return user;
    }

    /**
     * Get user by ID
     */
    public Optional<User> getUserById(int id) {
        log.info("Fetching user ID: {}", id);
        return userRepository.findById(id);
    }
}