package com.example.campus_events.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

/**
 * Utility class for JWT token generation and validation
 */
@Component
public class JwtUtil {

    private static final String SECRET = "evenzo_super_secret_key_2026_campus_events_jwt";
    private static final long EXPIRATION = 1000 * 60 * 60 * 24; // 24 hours

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    /**
     * Generate JWT token for a user
     */
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract email from token
     */
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extract role from token
     */
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    /**
     * Validate token
     */
    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("[WARN] Invalid JWT token: " + e.getMessage());
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}