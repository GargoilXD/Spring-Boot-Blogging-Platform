package com.blog.Service;

import com.blog.Repository.UserRepository;
import com.blog.DataTransporter.User.RegisterUserDTO;
import com.blog.Model.User;
import com.blog.Exception.AuthenticationException;
import com.blog.Utility.PasswordHasher;
import jakarta.persistence.EntityExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private User user;

    @InjectMocks
    private AuthenticationService authService;

    private RegisterUserDTO registerDTO;

    @BeforeEach
    void setUp() {
        registerDTO = new RegisterUserDTO("testuser", "password123", "Full Name", "test@example.com", "Male");
    }

    @Test
    void login_Success() {
        String username = "testuser";
        String password = "password123";
        String hashedPassword = "hashed_value";

        when(repository.findByUsername(username.trim())).thenReturn(Optional.of(user));
        when(user.getPasswordHash()).thenReturn(hashedPassword);
        when(passwordHasher.verifyPassword(password, hashedPassword)).thenReturn(true);

        assertDoesNotThrow(() -> authService.login(username, password));

        verify(repository).findByUsername(username.trim());
        verify(passwordHasher).verifyPassword(password, hashedPassword);
    }

    @Test
    void login_Failure_UserNotFound() {
        String username = "unknown";
        String password = "password123";

        when(repository.findByUsername(username.trim())).thenReturn(Optional.empty());

        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.login(username, password)
        );

        assertEquals("Invalid credentials", exception.getMessage());
        verify(repository).findByUsername(username.trim());
        verify(passwordHasher, never()).verifyPassword(anyString(), anyString());
    }

    @Test
    void login_Failure_WrongPassword() {
        String username = "testuser";
        String password = "wrong_password";
        String hashedPassword = "hashed_value";

        when(repository.findByUsername(username.trim())).thenReturn(Optional.of(user));
        when(user.getPasswordHash()).thenReturn(hashedPassword);
        when(passwordHasher.verifyPassword(password, hashedPassword)).thenReturn(false);

        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.login(username, password)
        );

        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void register_Success() {
        when(repository.findByUsername(registerDTO.username().trim())).thenReturn(Optional.empty());
        when(passwordHasher.hashPassword(registerDTO.password())).thenReturn("new_hash");
        // Mock the chain: DTO.withPasswordHash(...).toEntity()
        // Since we can't easily mock record methods, we assume the service calls save.
        // We verify save is called with any User object.
        when(repository.save(any(User.class))).thenReturn(user);

        assertDoesNotThrow(() -> authService.register(registerDTO));

        verify(repository).findByUsername(registerDTO.username().trim());
        verify(passwordHasher).hashPassword(registerDTO.password());
        verify(repository).save(any(User.class));
    }

    @Test
    void register_Failure_UserExists() {
        when(repository.findByUsername(registerDTO.username().trim())).thenReturn(Optional.of(user));

        EntityExistsException exception = assertThrows(
                EntityExistsException.class,
                () -> authService.register(registerDTO)
        );

        assertTrue(exception.getMessage().contains("Username already exists"));
        verify(repository, never()).save(any());
    }
}