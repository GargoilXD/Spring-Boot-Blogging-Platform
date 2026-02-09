package com.blog.API.Rest;

import com.blog.DataTransporter.User.LoginUserDTO;
import com.blog.DataTransporter.User.RegisterUserDTO;
import com.blog.Service.AuthenticationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class RestAuthenticationController {
    private final AuthenticationService authService;

    public RestAuthenticationController(AuthenticationService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginUserDTO request) {
        authService.authenticate(request.username(), request.password());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterUserDTO request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}