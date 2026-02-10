package com.blog.API.Rest;

import com.blog.DataTransporter.User.LoginUserDTO;
import com.blog.DataTransporter.User.RegisterUserDTO;
import com.blog.Service.AuthenticationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs for user login and registration")
public class RestAuthenticationController {
    private final AuthenticationService authService;

    public RestAuthenticationController(AuthenticationService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(
        summary = "Authenticate user",
        description = "Authenticates a user with username and password credentials"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "202",
            description = "User authenticated successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request body",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<Void> login(@RequestBody LoginUserDTO request) {
        authService.authenticate(request.username(), request.password());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
    @PostMapping("/register")
    @Operation(
        summary = "Register new user",
        description = "Creates a new user account with the provided registration details"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User registered successfully"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "User already exists",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid registration data",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<Void> register(@RequestBody RegisterUserDTO request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}