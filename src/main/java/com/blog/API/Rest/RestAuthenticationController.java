package com.blog.API.Rest;

import com.blog.API.Response.SuccessResponse;
import com.blog.DataTransporter.User.LoginUserDTO;
import com.blog.DataTransporter.User.RegisterUserDTO;
import com.blog.Service.AuthenticationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication APIs for user registration, login, and logout")
public class RestAuthenticationController {

    private final AuthenticationService authService;

    public RestAuthenticationController(AuthenticationService authService) {
        this.authService = authService;
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @PostMapping("/login")
    @Operation(
        summary = "Authenticate user",
        description = "Authenticates a user with username and password. Returns a signed JWT token " +
                      "to include as 'Authorization: Bearer <token>' on subsequent requests."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Authenticated — JWT token returned"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "400", description = "Malformed request body")
    })
    public ResponseEntity<SuccessResponse<Map<String, String>>> login(@RequestBody LoginUserDTO request) {
        String token = authService.login(request.username(), request.password());
        Map<String, String> data = Map.of("token", token, "type", "Bearer");
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new SuccessResponse<>(HttpStatus.ACCEPTED, "User authenticated successfully", data));
    }

    // ── Register ──────────────────────────────────────────────────────────────

    @PostMapping("/register")
    @Operation(
        summary = "Register new user",
        description = "Creates a new user account. Password is hashed with Argon2id before storage. " +
                      "New users receive the READER role by default."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User registered successfully"),
        @ApiResponse(responseCode = "409", description = "Username already exists"),
        @ApiResponse(responseCode = "400", description = "Invalid registration data")
    })
    public ResponseEntity<SuccessResponse<Void>> register(@RequestBody RegisterUserDTO request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse<>(HttpStatus.CREATED, "User registered successfully"));
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @PostMapping("/logout")
    @Operation(
        summary = "Logout user",
        description = "Revokes the provided JWT token by adding it to the in-memory blacklist. " +
                      "The token will be rejected on all subsequent requests.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully logged out"),
        @ApiResponse(responseCode = "401", description = "Token missing or invalid")
    })
    public ResponseEntity<SuccessResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
        }
        return ResponseEntity.ok(new SuccessResponse<>(HttpStatus.OK, "Successfully logged out"));
    }
}
