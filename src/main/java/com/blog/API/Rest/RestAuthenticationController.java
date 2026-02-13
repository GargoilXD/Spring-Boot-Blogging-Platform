package com.blog.API.Rest;

import com.blog.API.Response.SuccessResponse;
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
@Tag(name = "Authentication", description = "Authentication management APIs for user registration, login, and session management")
public class RestAuthenticationController {
    private final AuthenticationService authService;

    public RestAuthenticationController(AuthenticationService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(
        summary = "Authenticate user",
        description = "Authenticates a user with username and password credentials. Returns a success response upon successful authentication.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "User login credentials",
            required = true,
            content = @Content(schema = @Schema(implementation = LoginUserDTO.class))
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "202",
            description = "User authenticated successfully",
            content = @Content(schema = @Schema(implementation = SuccessResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials - username or password is incorrect",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request body - missing or malformed fields",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<SuccessResponse<Void>> login(@RequestBody LoginUserDTO request) {
        authService.login(request.username(), request.password());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new SuccessResponse<>(HttpStatus.ACCEPTED, "User authenticated successfully"));
    }
    @PostMapping("/register")
    @Operation(
        summary = "Register new user",
        description = "Creates a new user account with the provided registration details. Password will be hashed before storage.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "User registration information",
            required = true,
            content = @Content(schema = @Schema(implementation = RegisterUserDTO.class))
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = SuccessResponse.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "User already exists - username or email is already registered",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid registration data - validation errors in request body",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<SuccessResponse<Void>> register(@RequestBody RegisterUserDTO request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponse<>(HttpStatus.CREATED, "User registered successfully"))    ;
    }
}