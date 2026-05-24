package com.example.campus_events.security;

import com.example.campus_events.model.User;
import com.example.campus_events.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class GoogleOAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.allowed-domain}")
    private String allowedDomain;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        // Domain restriction
        if (email == null || !email.endsWith("@" + allowedDomain)) {
            response.sendRedirect(frontendUrl + "/auth-error?reason=domain");
            return;
        }

        // Find or create user
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User(name, email, "", "STUDENT");
            newUser.setProfilePicture(picture);
            return userRepository.save(newUser);
        });

        // Update picture if changed
        if (picture != null && !picture.equals(user.getProfilePicture())) {
            user.setProfilePicture(picture);
            userRepository.save(user);
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        // Redirect to frontend with token
        response.sendRedirect(frontendUrl + "/auth/callback?token=" + token
                + "&id=" + user.getId()
                + "&name=" + java.net.URLEncoder.encode(user.getName(), "UTF-8")
                + "&email=" + user.getEmail()
                + "&role=" + user.getRole()
                + "&picture=" + java.net.URLEncoder.encode(picture != null ? picture : "", "UTF-8"));
    }
}