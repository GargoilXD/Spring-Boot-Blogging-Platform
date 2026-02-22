package com.blog.API.Rest;

import com.blog.API.Response.SuccessResponse;
import com.blog.DataTransporter.User.LoginUserDTO;
import com.blog.DataTransporter.User.RefreshRequestDTO;
import com.blog.DataTransporter.User.RegisterUserDTO;
import com.blog.Security.JwtService;
import com.blog.Service.AuthenticationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ──────────────────────────────────────────────────────────────
 *  Epic 2 – JWT Authentication Endpoints
 * ──────────────────────────────────────────────────────────────
 *
 *  POST /api/auth/login         — credentials → { accessToken, refreshToken }
 *  POST /api/auth/register      — create account (READER role)
 *  POST /api/auth/refresh       — refresh token → new { accessToken, refreshToken }
 *  POST /api/auth/logout        — revoke access + refresh tokens
 *  GET  /api/auth/token/inspect — decode and display JWT claims (for Postman verification)
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "JWT authentication: login, register, token refresh, logout, and token inspection")
public class RestAuthenticationController {

    private final AuthenticationService authService;
    private final JwtService jwtService;

    public RestAuthenticationController(AuthenticationService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService  = jwtService;
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @PostMapping("/login")
    @Operation(
        summary = "Authenticate user",
        description = "Verifies credentials with Argon2id password hashing. Returns a short-lived " +
                      "access token and a long-lived refresh token. Include the access token as " +
                      "'Authorization: Bearer <token>' on all subsequent requests."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Authenticated — access and refresh tokens returned"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "400", description = "Malformed request body")
    })
    public ResponseEntity<SuccessResponse<Map<String, String>>> login(
            @Valid @RequestBody LoginUserDTO request) {

        Map<String, String> tokens = authService.login(request.username(), request.password());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new SuccessResponse<>(HttpStatus.ACCEPTED, "User authenticated successfully", tokens));
    }

    // ── Register ──────────────────────────────────────────────────────────────

    @PostMapping("/register")
    @Operation(
        summary = "Register new user",
        description = "Creates a new account. Password is hashed with Argon2id before storage. " +
                      "New users receive the READER role by default."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User registered successfully"),
        @ApiResponse(responseCode = "409", description = "Username already exists"),
        @ApiResponse(responseCode = "400", description = "Invalid registration data")
    })
    public ResponseEntity<SuccessResponse<Void>> register(
            @Valid @RequestBody RegisterUserDTO request) {

        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse<>(HttpStatus.CREATED, "User registered successfully"));
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh access token",
        description = "Exchanges a valid refresh token for a new access + refresh token pair. " +
                      "Token rotation: the submitted refresh token is immediately revoked. " +
                      "Use this endpoint when the access token expires (HTTP 401)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "New token pair issued"),
        @ApiResponse(responseCode = "401", description = "Refresh token is invalid, expired, or revoked")
    })
    public ResponseEntity<SuccessResponse<Map<String, String>>> refresh(
            @Valid @RequestBody RefreshRequestDTO request) {

        Map<String, String> tokens = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK, "Tokens refreshed successfully", tokens));
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @PostMapping("/logout")
    @Operation(
        summary = "Logout user",
        description = "Revokes both the access token (added to blacklist) and the refresh token " +
                      "(evicted from the active store). Pass the refresh token in the request body.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully logged out"),
        @ApiResponse(responseCode = "401", description = "Access token missing or invalid")
    })
    public ResponseEntity<SuccessResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody(required = false) RefreshRequestDTO body) {

        String accessToken  = (authHeader != null && authHeader.startsWith("Bearer "))
                              ? authHeader.substring(7) : null;
        String refreshToken = (body != null) ? body.refreshToken() : null;

        authService.logout(accessToken, refreshToken);
        return ResponseEntity.ok(new SuccessResponse<>(HttpStatus.OK, "Successfully logged out"));
    }

    // ── Token Inspect ─────────────────────────────────────────────────────────

    @GetMapping("/token/inspect")
    @Operation(
        summary = "Inspect JWT token claims",
        description = "Decodes and returns all claims from the provided Bearer token without " +
                      "requiring full authentication. Useful for verifying token structure, " +
                      "roles, issue time, and expiration in Postman. (User Story 2.2)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Token claims decoded successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid or malformed token")
    })
    public ResponseEntity<SuccessResponse<Map<String, Object>>> inspectToken(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new SuccessResponse<>(HttpStatus.UNAUTHORIZED, "No Bearer token provided"));
        }

        String token = authHeader.substring(7);
        Map<String, Object> claims = jwtService.inspectToken(token);
        return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK, "Token decoded successfully", claims));
    }
}
