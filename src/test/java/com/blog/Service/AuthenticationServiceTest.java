package com.blog.Service;

import com.blog.DataAccessor.Interface.UserDataAccessor;
import com.blog.DataTransporter.User.RegisterUserDTO;
import com.blog.Model.User;
import com.blog.Service.Exception.AuthenticationException;
import com.blog.Service.Exception.UserAlreadyExistsException;
import com.blog.Utility.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserDataAccessor dataAccessor;

    @Mock
    private PasswordHasher passwordHasher;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private RegisterUserDTO registerUserDTO;
    private String hashedPassword;

    @BeforeEach
    void setUp() {
        hashedPassword = "$argon2id$v=19$m=65536,t=3,p=4$hashedPasswordExample";

        testUser = new User(
                1L,
                "johndoe",
                hashedPassword,
                "John Doe",
                "john.doe@example.com",
                "Male",
                "2024-01-15T10:30:00"
        );

        registerUserDTO = new RegisterUserDTO(
                "newuser",
                "SecurePass123!",
                "New User",
                "newuser@example.com",
                "Male"
        );
    }

    @Test
    void testAuthenticate_Success() {
        // Arrange
        when(dataAccessor.getByUsername("johndoe")).thenReturn(Optional.of(testUser));
        when(passwordHasher.verifyPassword("correctPassword", hashedPassword)).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> authenticationService.authenticate("johndoe", "correctPassword"));
        verify(dataAccessor, times(1)).getByUsername("johndoe");
        verify(passwordHasher, times(1)).verifyPassword("correctPassword", hashedPassword);
    }

    @Test
    void testAuthenticate_UserNotFound() {
        // Arrange
        when(dataAccessor.getByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authenticationService.authenticate("nonexistent", "password")
        );
        assertEquals("Invalid credentials", exception.getMessage());
        verify(dataAccessor, times(1)).getByUsername("nonexistent");
        verify(passwordHasher, never()).verifyPassword(anyString(), anyString());
    }

    @Test
    void testAuthenticate_InvalidPassword() {
        // Arrange
        when(dataAccessor.getByUsername("johndoe")).thenReturn(Optional.of(testUser));
        when(passwordHasher.verifyPassword("wrongPassword", hashedPassword)).thenReturn(false);

        // Act & Assert
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authenticationService.authenticate("johndoe", "wrongPassword")
        );
        assertEquals("Invalid credentials", exception.getMessage());
        verify(dataAccessor, times(1)).getByUsername("johndoe");
        verify(passwordHasher, times(1)).verifyPassword("wrongPassword", hashedPassword);
    }

    @Test
    void testAuthenticate_WithWhitespace() {
        // Arrange
        when(dataAccessor.getByUsername("johndoe")).thenReturn(Optional.of(testUser));
        when(passwordHasher.verifyPassword("correctPassword", hashedPassword)).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> authenticationService.authenticate("  johndoe  ", "correctPassword"));
        verify(dataAccessor, times(1)).getByUsername("johndoe");
        verify(passwordHasher, times(1)).verifyPassword("correctPassword", hashedPassword);
    }

    @Test
    void testRegister_Success() {
        // Arrange
        when(dataAccessor.getByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordHasher.hashPassword("SecurePass123!")).thenReturn(hashedPassword);
        doNothing().when(dataAccessor).register(any(RegisterUserDTO.class));

        // Act & Assert
        assertDoesNotThrow(() -> authenticationService.register(registerUserDTO));
        verify(dataAccessor, times(1)).getByUsername("newuser");
        verify(passwordHasher, times(1)).hashPassword("SecurePass123!");
        verify(dataAccessor, times(1)).register(any(RegisterUserDTO.class));
    }

    @Test
    void testRegister_UserAlreadyExists() {
        // Arrange
        when(dataAccessor.getByUsername("johndoe")).thenReturn(Optional.of(testUser));
        RegisterUserDTO existingUserDTO = new RegisterUserDTO(
                "johndoe",
                "SecurePass123!",
                "John Doe",
                "john@example.com",
                "Male"
        );

        // Act & Assert
        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> authenticationService.register(existingUserDTO)
        );
        assertTrue(exception.getMessage().contains("johndoe"));
        verify(dataAccessor, times(1)).getByUsername("johndoe");
        verify(passwordHasher, never()).hashPassword(anyString());
        verify(dataAccessor, never()).register(any(RegisterUserDTO.class));
    }

    @Test
    void testRegister_WithWhitespace() {
        // Arrange
        RegisterUserDTO dtoWithWhitespace = new RegisterUserDTO(
                "  newuser  ",
                "SecurePass123!",
                "New User",
                "newuser@example.com",
                "Male"
        );
        when(dataAccessor.getByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordHasher.hashPassword("SecurePass123!")).thenReturn(hashedPassword);
        doNothing().when(dataAccessor).register(any(RegisterUserDTO.class));

        // Act & Assert
        assertDoesNotThrow(() -> authenticationService.register(dtoWithWhitespace));
        verify(dataAccessor, times(1)).getByUsername("newuser");
        verify(passwordHasher, times(1)).hashPassword("SecurePass123!");
        verify(dataAccessor, times(1)).register(any(RegisterUserDTO.class));
    }

    @Test
    void testRegister_InvalidUsername() {
        // Arrange
        RegisterUserDTO invalidDTO = new RegisterUserDTO(
                "ab",  // Too short
                "SecurePass123!",
                "User",
                "user@example.com",
                "Male"
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authenticationService.register(invalidDTO)
        );
        assertTrue(exception.getMessage().contains("Username"));
        verify(dataAccessor, never()).getByUsername(anyString());
        verify(passwordHasher, never()).hashPassword(anyString());
        verify(dataAccessor, never()).register(any(RegisterUserDTO.class));
    }

    @Test
    void testRegister_InvalidPassword() {
        // Arrange
        RegisterUserDTO invalidDTO = new RegisterUserDTO(
                "validuser",
                "short",  // Too short
                "Valid User",
                "valid@example.com",
                "Male"
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authenticationService.register(invalidDTO)
        );
        assertTrue(exception.getMessage().contains("Password"));
        verify(dataAccessor, never()).getByUsername(anyString());
        verify(passwordHasher, never()).hashPassword(anyString());
        verify(dataAccessor, never()).register(any(RegisterUserDTO.class));
    }

    @Test
    void testRegister_InvalidEmail() {
        // Arrange
        RegisterUserDTO invalidDTO = new RegisterUserDTO(
                "validuser",
                "SecurePass123!",
                "Valid User",
                "invalid-email",  // Invalid email format
                "Male"
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authenticationService.register(invalidDTO)
        );
        assertTrue(exception.getMessage().contains("email"));
        verify(dataAccessor, never()).getByUsername(anyString());
        verify(passwordHasher, never()).hashPassword(anyString());
        verify(dataAccessor, never()).register(any(RegisterUserDTO.class));
    }
}
