package com.example.campus_events.security;

import com.example.campus_events.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private UserRepository userRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // ✅ PUBLIC ENDPOINTS
                        .requestMatchers("/api/users/register").permitAll()
                        .requestMatchers("/api/users/login").permitAll()
                        .requestMatchers("/api/events").permitAll()
                        .requestMatchers("/api/events/{id}").permitAll()
                        .requestMatchers("/", "/index.html", "/**/*.js", "/**/*.css").permitAll()

                        // ✅ AUTH REQUIRED - EVENTS
                        .requestMatchers("/api/events/*/register").authenticated()
                        .requestMatchers("/api/events/*/participants").authenticated()

                        // 🔥 NEWLY ADDED (YOUR PART)
                        .requestMatchers("/api/wallet/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/events/*/status").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/events/*/waitlist").authenticated()
                        .requestMatchers("/api/events/*/image").authenticated()

                        // ✅ FALLBACK
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            com.example.campus_events.model.User user =
                    userRepository.findByEmail(username)
                            .orElseThrow(() ->
                                    new UsernameNotFoundException("User not found: " + username));

            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPassword())
                    .roles(user.getRole().toUpperCase())
                    .build();
        };
    }
}