package com.blog.API.Rest;

import com.blog.API.Response.SuccessResponse;
import com.blog.DataTransporter.User.LoginUserDTO;
import com.blog.DataTransporter.User.RegisterUserDTO;
import com.blog.Service.AuthenticationService;
import com.blog.Exception.AuthenticationException;
import jakarta.persistence.EntityExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestAuthenticationControllerTest {

    @Mock
    private AuthenticationService authService;

    @InjectMocks
    private RestAuthenticationController authController;

    private LoginUserDTO loginDTO;
    private RegisterUserDTO registerDTO;

    @BeforeEach
    void setUp() {
        loginDTO = new LoginUserDTO("user", "password123");
        registerDTO = new RegisterUserDTO("user", "password123", "Full Name", "user@example.com", "Male");
    }

    @Test
    void login_Success() {
        doNothing().when(authService).login(loginDTO.username(), loginDTO.password());

        ResponseEntity<SuccessResponse<Void>> response = authController.login(loginDTO);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(authService).login(loginDTO.username(), loginDTO.password());
    }

    @Test
    void login_Failure_PropagatesException() {
        doThrow(new AuthenticationException("Invalid credentials"))
                .when(authService).login(anyString(), anyString());

        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authController.login(loginDTO)
        );

        assertEquals("Invalid credentials", exception.getMessage());
        verify(authService).login(loginDTO.username(), loginDTO.password());
    }

    @Test
    void register_Success() {
        doNothing().when(authService).register(registerDTO);

        ResponseEntity<SuccessResponse<Void>> response = authController.register(registerDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(authService).register(registerDTO);
    }

    @Test
    void register_Failure_PropagatesException() {
        doThrow(new EntityExistsException("Username already exists"))
                .when(authService).register(any(RegisterUserDTO.class));

        EntityExistsException exception = assertThrows(
                EntityExistsException.class,
                () -> authController.register(registerDTO)
        );

        assertTrue(exception.getMessage().contains("Username already exists"));
        verify(authService).register(registerDTO);
    }
}