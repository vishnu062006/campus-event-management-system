package com.example.campus_events;

import com.example.campus_events.model.User;
import com.example.campus_events.repository.UserRepository;
import com.example.campus_events.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testRegisterSuccess() {
        when(userRepository.existsByEmail("vishnu@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User user = userService.register("Vishnu", "vishnu@example.com", "pass123", "student");

        assertEquals("Vishnu", user.getName());
        assertEquals("vishnu@example.com", user.getEmail());
        assertEquals("student", user.getRole());
    }

    @Test
    void testRegisterDuplicateEmail() {
        when(userRepository.existsByEmail("vishnu@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                userService.register("Vishnu", "vishnu@example.com", "pass123", "student")
        );
    }

    @Test
    void testRegisterEmptyName() {
        assertThrows(IllegalArgumentException.class, () ->
                userService.register("", "vishnu@example.com", "pass123", "student")
        );
    }

    @Test
    void testRegisterShortPassword() {
        assertThrows(IllegalArgumentException.class, () ->
                userService.register("Vishnu", "vishnu@example.com", "123", "student")
        );
    }

    @Test
    void testLoginSuccess() {
        User mockUser = new User("Vishnu", "vishnu@example.com", "pass123", "student");
        when(userRepository.findByEmail("vishnu@example.com")).thenReturn(Optional.of(mockUser));

        User user = userService.login("vishnu@example.com", "pass123");

        assertEquals("Vishnu", user.getName());
    }

    @Test
    void testLoginWrongPassword() {
        User mockUser = new User("Vishnu", "vishnu@example.com", "pass123", "student");
        when(userRepository.findByEmail("vishnu@example.com")).thenReturn(Optional.of(mockUser));

        assertThrows(IllegalArgumentException.class, () ->
                userService.login("vishnu@example.com", "wrongpass")
        );
    }

    @Test
    void testLoginUserNotFound() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                userService.login("nobody@example.com", "pass123")
        );
    }
}