package com.blog.API.Rest;

import com.blog.DataAccessor.Exception.DataAccessException;
import com.blog.DataTransporter.UserDataTransporter;
import com.blog.Model.User;
import com.blog.Service.AuthenticationService;
import com.blog.Service.Exception.AuthenticationException;
import com.blog.Service.Exception.UserAlreadyExistsException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class RestAuthenticationController {
    public record LoginRequest(String username, String password) {}

    private final AuthenticationService authService;

    public RestAuthenticationController(AuthenticationService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<UserDataTransporter> login(@RequestBody LoginRequest request) {
        try {
            User user = authService.authenticate(request.username(), request.password());
            return ResponseEntity.ok(UserDataTransporter.fromEntity(user));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody UserDataTransporter udto) {
        try {
            authService.register(udto);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}