package com.example.campus_events.controller;

import com.example.campus_events.model.User;
import com.example.campus_events.security.JwtUtil;
import com.example.campus_events.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * REST Controller for User endpoints
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * POST /api/users/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String email = body.get("email");
        String password = body.get("password");
        String role = body.getOrDefault("role", "student");
        User user = userService.register(name, email, password, role);
        return ResponseEntity.status(201).body(user);
    }

    /**
     * POST /api/users/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        User user = userService.login(email, password);
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        System.out.println("[INFO] JWT generated for: " + email);
        return ResponseEntity.ok(Map.of(
                "token", token,
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole()
        ));
    }

    /**
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable int id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}