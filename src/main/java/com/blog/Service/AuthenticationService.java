package com.blog.Service;

import com.blog.Repository.UserRepository;
import com.blog.DataTransporter.User.RegisterUserDTO;
import com.blog.Model.User;
import com.blog.Exception.AuthenticationException;
import com.blog.Security.BlogUserDetailsService;
import com.blog.Security.JwtService;
import com.blog.Security.TokenBlacklistService;
import com.blog.Utility.PasswordHasher;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Handles user authentication (login/register) and JWT lifecycle.
 *
 * Flow:
 *  login    → verify Argon2 hash → load UserDetails → generate JWT → return token
 *  register → check duplicates → hash password → save user
 *  logout   → revoke token via blacklist
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordHasher passwordHasher;
    private final JwtService jwtService;
    private final BlogUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Authenticates the user and returns a signed JWT.
     *
     * @throws AuthenticationException if username not found or password mismatch
     */
    public String login(String username, String password) {
        User user = repository.findByUsername(username.trim())
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        if (!passwordHasher.verifyPassword(password, user.getPasswordHash())) {
            throw new AuthenticationException("Invalid credentials");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username.trim());
        return jwtService.generateToken(userDetails);
    }

    /**
     * Registers a new user with a hashed password.
     * Default role is READER — can be upgraded to AUTHOR or ADMIN by an admin.
     */
    public void register(RegisterUserDTO dto) {
        if (repository.findByUsername(dto.username().trim()).isPresent()) {
            throw new EntityExistsException("Username already exists: " + dto.username());
        }
        repository.save(dto.withPasswordHash(passwordHasher.hashPassword(dto.password())).toEntity());
    }

    /**
     * Revokes the given JWT by adding it to the in-memory blacklist.
     */
    public void logout(String token) {
        tokenBlacklistService.revoke(token);
    }
}
