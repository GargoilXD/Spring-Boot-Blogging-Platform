package com.blog.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.blog.DataTransporter.User.RegisterUserDTO;
import com.blog.Exception.AuthenticationException;
import com.blog.Model.User;
import com.blog.Repository.UserRepository;
import com.blog.Utility.PasswordHasher;

import jakarta.persistence.EntityExistsException;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService Tests")
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @InjectMocks
    private AuthenticationService authenticationService;

    private long testStartTime;
    private long testEndTime;

    @BeforeEach
    void setUp() {
        testStartTime = System.nanoTime();
    }

    @AfterEach
    void tearDown() {
        testEndTime = System.nanoTime();
        long executionTimeMs = (testEndTime - testStartTime) / 1_000_000;
        System.out.println("Execution Time: " + executionTimeMs + " ms");
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void testLoginSuccess() {
        // Arrange
        String username = "john_doe";
        String password = "SecurePass123!";
        User user = new User(1, username, "hashedPassword", "John Doe", "john@example.com", "Male", LocalDateTime.now());

        when(userRepository.findByUsername(username.trim())).thenReturn(Optional.of(user));
        when(passwordHasher.verifyPassword(password, user.getPasswordHash())).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> authenticationService.login(username, password));

        verify(userRepository, times(1)).findByUsername(username.trim());
        verify(passwordHasher, times(1)).verifyPassword(password, user.getPasswordHash());
    }

    @Test
    @DisplayName("Should throw exception when user not found during login")
    void testLoginUserNotFound() {
        // Arrange
        String username = "nonexistent_user";
        String password = "SecurePass123!";

        when(userRepository.findByUsername(username.trim())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> authenticationService.login(username, password));

        verify(userRepository, times(1)).findByUsername(username.trim());
        verify(passwordHasher, never()).verifyPassword(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when password is incorrect")
    void testLoginInvalidPassword() {
        // Arrange
        String username = "john_doe";
        String password = "WrongPassword123!";
        User user = new User(1, username, "hashedPassword", "John Doe", "john@example.com", "Male", LocalDateTime.now());

        when(userRepository.findByUsername(username.trim())).thenReturn(Optional.of(user));
        when(passwordHasher.verifyPassword(password, user.getPasswordHash())).thenReturn(false);

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> authenticationService.login(username, password));

        verify(userRepository, times(1)).findByUsername(username.trim());
        verify(passwordHasher, times(1)).verifyPassword(password, user.getPasswordHash());
    }

    @Test
    @DisplayName("Should successfully register a new user")
    void testRegisterSuccess() {
        // Arrange
        RegisterUserDTO dto = new RegisterUserDTO("newuser", "SecurePass123!", "New User", "new@example.com", "Female");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordHasher.hashPassword("SecurePass123!")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // Act
        assertDoesNotThrow(() -> authenticationService.register(dto));

        // Assert
        verify(userRepository, times(1)).findByUsername("newuser");
        verify(passwordHasher, times(1)).hashPassword("SecurePass123!");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when username already exists during registration")
    void testRegisterUserAlreadyExists() {
        // Arrange
        RegisterUserDTO dto = new RegisterUserDTO("existinguser", "SecurePass123!", "Existing User", "existing@example.com", "Male");
        User existingUser = new User(1, "existinguser", "hashedPassword", "Existing User", "existing@example.com", "Male", LocalDateTime.now());

        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThrows(EntityExistsException.class, () -> authenticationService.register(dto));

        verify(userRepository, times(1)).findByUsername("existinguser");
        verify(passwordHasher, never()).hashPassword(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should trim username during login")
    void testLoginTrimsUsername() {
        // Arrange
        String usernameWithSpaces = "  john_doe  ";
        String password = "SecurePass123!";
        User user = new User(1, "john_doe", "hashedPassword", "John Doe", "john@example.com", "Male", LocalDateTime.now());

        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(user));
        when(passwordHasher.verifyPassword(password, user.getPasswordHash())).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> authenticationService.login(usernameWithSpaces, password));

        verify(userRepository, times(1)).findByUsername("john_doe");
    }

    @Test
    @DisplayName("Should trim username during registration")
    void testRegisterTrimsUsername() {
        // Arrange
        RegisterUserDTO dto = new RegisterUserDTO("   newuser   ", "SecurePass123!", "New User", "new@example.com", "Female");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordHasher.hashPassword("SecurePass123!")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // Act
        assertDoesNotThrow(() -> authenticationService.register(dto));

        // Assert
        verify(userRepository, times(1)).findByUsername("newuser");
    }
}
