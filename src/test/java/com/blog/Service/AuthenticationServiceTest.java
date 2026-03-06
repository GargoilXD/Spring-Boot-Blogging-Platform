package com.blog.Service;

import com.blog.DataTransporter.User.RegisterUserDTO;
import com.blog.Exception.AuthenticationException;
import com.blog.Model.Role;
import com.blog.Model.User;
import com.blog.Repository.UserRepository;
import com.blog.Security.BlogUserDetailsService;
import com.blog.Security.JwtService;
import com.blog.Security.RefreshTokenService;
import com.blog.Security.TokenBlacklistService;
import com.blog.Utility.PasswordHasher;
import jakarta.persistence.EntityExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    @Mock private UserRepository         repository;
    @Mock private PasswordHasher         passwordHasher;
    @Mock private JwtService             jwtService;
    @Mock private BlogUserDetailsService userDetailsService;
    @Mock private TokenBlacklistService  tokenBlacklistService;
    @Mock private RefreshTokenService    refreshTokenService;

    @InjectMocks
    private AuthenticationService authService;

    private User        testUser;
    private UserDetails testUserDetails;
    private RegisterUserDTO registerDTO;

    @BeforeEach
    void setUp() {
        testUser = new User(1, "testuser", "hashed_pw", "Test User", "test@example.com", "M", LocalDateTime.now(), Role.READER, new ArrayList<>(), new ArrayList<>());
        testUserDetails = org.springframework.security.core.userdetails.User
                .withUsername("testuser")
                .password("hashed_pw")
                .authorities(new SimpleGrantedAuthority("ROLE_READER"))
                .build();
        registerDTO = new RegisterUserDTO("testuser", "password123", "Test User", "test@example.com", "Male", Role.READER);
    }
    @Nested
    @DisplayName("Login")
    class LoginTests {
        @Test
        @DisplayName("Successful login returns access and refresh tokens")
        void login_Success_ReturnsTokenMap() {
            when(repository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(passwordHasher.verifyPassword("password123", "hashed_pw")).thenReturn(true);
            when(userDetailsService.loadUserByUsername("testuser")).thenReturn(testUserDetails);
            when(jwtService.generateAccessToken(testUserDetails)).thenReturn("access.token.here");
            when(jwtService.generateRefreshToken(testUserDetails)).thenReturn("refresh.token.here");
            Map<String, String> result = authService.login("testuser", "password123");
            assertAll(
                () -> assertEquals("access.token.here",  result.get("accessToken")),
                () -> assertEquals("refresh.token.here", result.get("refreshToken")),
                () -> assertEquals("Bearer",             result.get("type"))
            );
            verify(refreshTokenService).store("refresh.token.here", "testuser");
        }
        @Test
        @DisplayName("Login with unknown username throws AuthenticationException")
        void login_UserNotFound_ThrowsException() {
            when(repository.findByUsername(anyString())).thenReturn(Optional.empty());
            AuthenticationException ex = assertThrows(AuthenticationException.class, () -> authService.login("unknown", "password123"));
            assertEquals("Invalid credentials", ex.getMessage());
            verify(passwordHasher, never()).verifyPassword(anyString(), anyString());
        }
        @Test
        @DisplayName("Login with wrong password throws AuthenticationException")
        void login_WrongPassword_ThrowsException() {
            when(repository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(passwordHasher.verifyPassword("wrong", "hashed_pw")).thenReturn(false);
            AuthenticationException ex = assertThrows(AuthenticationException.class, () -> authService.login("testuser", "wrong"));
            assertEquals("Invalid credentials", ex.getMessage());
        }
        @Test
        @DisplayName("Login trims leading/trailing spaces from username")
        void login_TrimsUsername() {
            when(repository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(passwordHasher.verifyPassword(anyString(), anyString())).thenReturn(true);
            when(userDetailsService.loadUserByUsername("testuser")).thenReturn(testUserDetails);
            when(jwtService.generateAccessToken(any())).thenReturn("access");
            when(jwtService.generateRefreshToken(any())).thenReturn("refresh");

            assertDoesNotThrow(() -> authService.login("  testuser  ", "password123"));
            verify(repository).findByUsername("testuser"); // trimmed
        }
    }
    @Nested
    @DisplayName("Register")
    class RegisterTests {
        @Test
        @DisplayName("Successful registration saves hashed user")
        void register_Success() {
            when(repository.findByUsername("testuser")).thenReturn(Optional.empty());
            when(passwordHasher.hashPassword("password123")).thenReturn("new_hash");
            when(repository.save(any(User.class))).thenReturn(testUser);
            assertDoesNotThrow(() -> authService.register(registerDTO));
            verify(passwordHasher).hashPassword("password123");
            verify(repository).save(any(User.class));
        }
        @Test
        @DisplayName("Duplicate username throws EntityExistsException")
        void register_DuplicateUsername_ThrowsException() {
            when(repository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            EntityExistsException ex = assertThrows(EntityExistsException.class, () -> authService.register(registerDTO));
            assertTrue(ex.getMessage().contains("Username already exists"));
            verify(repository, never()).save(any());
        }
    }
    @Nested
    @DisplayName("Refresh Token")
    class RefreshTests {
        @Test
        @DisplayName("Valid refresh token returns new token pair")
        void refresh_ValidToken_ReturnsNewTokens() {
            when(jwtService.isTokenExpired("refresh.token")).thenReturn(false);
            when(jwtService.extractUsername("refresh.token")).thenReturn("testuser");
            when(refreshTokenService.isValid("refresh.token", "testuser")).thenReturn(true);
            when(jwtService.isRefreshTokenValid("refresh.token", "testuser")).thenReturn(true);
            when(userDetailsService.loadUserByUsername("testuser")).thenReturn(testUserDetails);
            when(jwtService.generateAccessToken(testUserDetails)).thenReturn("new.access.token");
            when(jwtService.generateRefreshToken(testUserDetails)).thenReturn("new.refresh.token");
            Map<String, String> result = authService.refresh("refresh.token");
            assertAll(
                () -> assertEquals("new.access.token",  result.get("accessToken")),
                () -> assertEquals("new.refresh.token", result.get("refreshToken"))
            );
            verify(refreshTokenService).evict("refresh.token");
            verify(refreshTokenService).store("new.refresh.token", "testuser");
        }
        @Test
        @DisplayName("Expired refresh token throws AuthenticationException")
        void refresh_ExpiredToken_ThrowsException() {
            when(jwtService.isTokenExpired("expired.token")).thenReturn(true);
            AuthenticationException ex = assertThrows(AuthenticationException.class, () -> authService.refresh("expired.token"));
            assertTrue(ex.getMessage().contains("Refresh token has expired"));
        }
        @Test
        @DisplayName("Unknown/evicted refresh token throws AuthenticationException")
        void refresh_RevokedToken_ThrowsException() {
            when(jwtService.isTokenExpired("revoked.token")).thenReturn(false);
            when(jwtService.extractUsername("revoked.token")).thenReturn("testuser");
            when(refreshTokenService.isValid("revoked.token", "testuser")).thenReturn(false);
            AuthenticationException ex = assertThrows(AuthenticationException.class, () -> authService.refresh("revoked.token"));
            assertTrue(ex.getMessage().contains("invalid or has been revoked"));
        }
    }
    @Nested
    @DisplayName("Logout")
    class LogoutTests {
        @Test
        @DisplayName("Logout blacklists access token and evicts refresh token")
        void logout_BlacklistsAndEvicts() {
            authService.logout("access.token", "refresh.token");
            verify(tokenBlacklistService).revoke("access.token");
            verify(refreshTokenService).evict("refresh.token");
        }
        @Test
        @DisplayName("Logout with null refresh token only blacklists access token")
        void logout_NullRefreshToken_OnlyBlacklists() {
            authService.logout("access.token", null);
            verify(tokenBlacklistService).revoke("access.token");
            verify(refreshTokenService, never()).evict(anyString());
        }
    }
}
