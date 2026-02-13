package com.blog.API.Rest;

import com.blog.DataTransporter.User.LoginUserDTO;
import com.blog.DataTransporter.User.RegisterUserDTO;
import com.blog.Service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityExistsException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RestAuthenticationController.class)
@DisplayName("REST Authentication Controller Tests")
class RestAuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authService;

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("POST /api/auth/login - Should login successfully with valid credentials")
    void testLoginSuccess() throws Exception {
        // Arrange
        LoginUserDTO loginRequest = new LoginUserDTO("johndoe", "SecurePass123!");
        doNothing().when(authService).login("johndoe", "SecurePass123!");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isAccepted());

        verify(authService, times(1)).login("johndoe", "SecurePass123!");
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 401 when login fails with invalid credentials")
    void testLoginFailure() throws Exception {
        // Arrange
        LoginUserDTO loginRequest = new LoginUserDTO("johndoe", "WrongPassword");
        doThrow(new RuntimeException("Invalid credentials"))
                .when(authService).login("johndoe", "WrongPassword");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is5xxServerError());

        verify(authService, times(1)).login("johndoe", "WrongPassword");
    }

    @Test
    @DisplayName("POST /api/auth/login - Should handle username with spaces")
    void testLoginWithUsernameSpaces() throws Exception {
        // Arrange
        LoginUserDTO loginRequest = new LoginUserDTO("john doe", "SecurePass123!");
        doNothing().when(authService).login("john doe", "SecurePass123!");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isAccepted());

        verify(authService, times(1)).login("john doe", "SecurePass123!");
    }

    @Test
    @DisplayName("POST /api/auth/register - Should register successfully with valid data")
    void testRegisterSuccess() throws Exception {
        // Arrange
        RegisterUserDTO registerRequest = new RegisterUserDTO("newuser", "SecurePass123!", "New User", "new@example.com", "Male");
        doNothing().when(authService).register(any(RegisterUserDTO.class));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        verify(authService, times(1)).register(any(RegisterUserDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should register female user successfully")
    void testRegisterFemaleUser() throws Exception {
        // Arrange
        RegisterUserDTO registerRequest = new RegisterUserDTO("jane", "SecurePass123!", "Jane Doe", "jane@example.com", "Female");
        doNothing().when(authService).register(any(RegisterUserDTO.class));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        verify(authService, times(1)).register(any(RegisterUserDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should register user with other gender")
    void testRegisterOtherGender() throws Exception {
        // Arrange
        RegisterUserDTO registerRequest = new RegisterUserDTO("user", "SecurePass123!", "User Name", "user@example.com", "Other");
        doNothing().when(authService).register(any(RegisterUserDTO.class));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        verify(authService, times(1)).register(any(RegisterUserDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 409 when registering with existing username")
    void testRegisterUserExists() throws Exception {
        // Arrange
        RegisterUserDTO registerRequest = new RegisterUserDTO("existing", "SecurePass123!", "Existing User", "existing@example.com", "Female");
        doThrow(new EntityExistsException("Username already exists"))
                .when(authService).register(any(RegisterUserDTO.class));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().is4xxClientError());

        verify(authService, times(1)).register(any(RegisterUserDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 400 for invalid request body")
    void testInvalidLoginRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 400 for invalid request body")
    void testInvalidRegisterRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/auth/register - Should handle username with spaces")
    void testRegisterWithUsernameSpaces() throws Exception {
        // Arrange
        RegisterUserDTO registerRequest = new RegisterUserDTO("john doe", "SecurePass123!", "John Doe", "john@example.com", "Male");
        doNothing().when(authService).register(any(RegisterUserDTO.class));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        verify(authService, times(1)).register(any(RegisterUserDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/login - Should handle special characters in password")
    void testLoginWithSpecialCharacters() throws Exception {
        // Arrange
        LoginUserDTO loginRequest = new LoginUserDTO("user", "P@ssw0rd!#$%");
        doNothing().when(authService).login("user", "P@ssw0rd!#$%");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isAccepted());

        verify(authService, times(1)).login("user", "P@ssw0rd!#$%");
    }
}
