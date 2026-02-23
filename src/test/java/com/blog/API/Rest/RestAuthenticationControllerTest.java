package com.blog.API.Rest;

import com.blog.API.Response.SuccessResponse;
import com.blog.DataTransporter.User.LoginUserDTO;
import com.blog.DataTransporter.User.RefreshRequestDTO;
import com.blog.DataTransporter.User.RegisterUserDTO;
import com.blog.Exception.AuthenticationException;
import com.blog.Security.JwtService;
import com.blog.Service.AuthenticationService;
import jakarta.persistence.EntityExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RestAuthenticationController Unit Tests")
class RestAuthenticationControllerTest {
    @Mock private AuthenticationService authService;
    @Mock private JwtService            jwtService;

    @InjectMocks
    private RestAuthenticationController authController;

    private LoginUserDTO    loginDTO;
    private RegisterUserDTO registerDTO;

    @BeforeEach
    void setUp() {
        loginDTO    = new LoginUserDTO("user", "password123");
        registerDTO = new RegisterUserDTO("user", "password123", "Full Name", "user@example.com", "Male");
    }
    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {
        @Test
        @DisplayName("Returns 202 with tokens on successful login")
        void login_Success() {
            Map<String, String> tokens = Map.of("accessToken", "acc", "refreshToken", "ref", "type", "Bearer");
            when(authService.login(loginDTO.username(), loginDTO.password())).thenReturn(tokens);
            ResponseEntity<SuccessResponse<Map<String, String>>> response = authController.login(loginDTO);
            assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(authService).login(loginDTO.username(), loginDTO.password());
        }
        @Test
        @DisplayName("Propagates AuthenticationException on bad credentials")
        void login_Failure_InvalidCredentials() {
            doThrow(new AuthenticationException("Invalid credentials")).when(authService).login(anyString(), anyString());
            AuthenticationException ex = assertThrows(AuthenticationException.class, () -> authController.login(loginDTO));
            assertEquals("Invalid credentials", ex.getMessage());
        }
    }
    @Nested
    @DisplayName("POST /api/auth/register")
    class Register {
        @Test
        @DisplayName("Returns 201 on successful registration")
        void register_Success() {
            doNothing().when(authService).register(registerDTO);
            ResponseEntity<SuccessResponse<Void>> response = authController.register(registerDTO);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            verify(authService).register(registerDTO);
        }
        @Test
        @DisplayName("Propagates EntityExistsException on duplicate username")
        void register_Failure_DuplicateUsername() {
            doThrow(new EntityExistsException("Username already exists")).when(authService).register(any(RegisterUserDTO.class));
            EntityExistsException ex = assertThrows(EntityExistsException.class, () -> authController.register(registerDTO));
            assertTrue(ex.getMessage().contains("Username already exists"));
        }
    }
    @Nested
    @DisplayName("POST /api/auth/refresh")
    class Refresh {
        @Test
        @DisplayName("Returns 200 with new token pair on valid refresh token")
        void refresh_Success() {
            RefreshRequestDTO refreshDTO = new RefreshRequestDTO("valid.refresh.token");
            Map<String, String> newTokens = Map.of("accessToken", "new.acc", "refreshToken", "new.ref", "type", "Bearer");
            when(authService.refresh("valid.refresh.token")).thenReturn(newTokens);
            ResponseEntity<SuccessResponse<Map<String, String>>> response = authController.refresh(refreshDTO);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(authService).refresh("valid.refresh.token");
        }

        @Test
        @DisplayName("Propagates AuthenticationException when refresh token is expired")
        void refresh_Failure_ExpiredToken() {
            RefreshRequestDTO refreshDTO = new RefreshRequestDTO("expired.token");
            doThrow(new AuthenticationException("Refresh token has expired")).when(authService).refresh("expired.token");
            AuthenticationException ex = assertThrows(AuthenticationException.class, () -> authController.refresh(refreshDTO));
            assertTrue(ex.getMessage().contains("Refresh token has expired"));
        }
    }
    @Nested
    @DisplayName("POST /api/auth/logout")
    class Logout {
        @Test
        @DisplayName("Returns 200 and delegates revocation to service")
        void logout_Success() {
            doNothing().when(authService).logout(anyString(), anyString());
            ResponseEntity<SuccessResponse<Void>> response = authController.logout("Bearer access.token", new RefreshRequestDTO("refresh.token"));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(authService).logout("access.token", "refresh.token");
        }
        @Test
        @DisplayName("Returns 200 when no refresh token body is provided")
        void logout_NoRefreshToken() {
            doNothing().when(authService).logout(anyString(), isNull());
            ResponseEntity<SuccessResponse<Void>> response = authController.logout("Bearer access.token", null);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(authService).logout("access.token", null);
        }
    }
    @Nested
    @DisplayName("GET /api/auth/token/inspect")
    class InspectToken {
        @Test
        @DisplayName("Returns 200 with decoded claims for a valid Bearer token")
        void inspectToken_Success() {
            Map<String, Object> claims = Map.of("subject", "user", "type", "access");
            when(jwtService.inspectToken("valid.token")).thenReturn(claims);
            ResponseEntity<SuccessResponse<Map<String, Object>>> response = authController.inspectToken("Bearer valid.token");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(jwtService).inspectToken("valid.token");
        }

        @Test
        @DisplayName("Returns 401 when Authorization header has no Bearer prefix")
        void inspectToken_Failure_NoBearerPrefix() {
            ResponseEntity<SuccessResponse<Map<String, Object>>> response = authController.inspectToken("BadHeader");
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            verifyNoInteractions(jwtService);
        }
        @Test
        @DisplayName("Returns 401 when Authorization header is null")
        void inspectToken_Failure_NullHeader() {
            ResponseEntity<SuccessResponse<Map<String, Object>>> response = authController.inspectToken(null);
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            verifyNoInteractions(jwtService);
        }
    }
}
