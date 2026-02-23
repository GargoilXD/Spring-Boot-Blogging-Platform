package com.blog.Service;

import com.blog.Exception.AuthenticationException;
import com.blog.DataTransporter.User.RegisterUserDTO;
import com.blog.Model.User;
import com.blog.Repository.UserRepository;
import com.blog.Security.BlogUserDetailsService;
import com.blog.Security.JwtService;
import com.blog.Security.RefreshTokenService;
import com.blog.Security.TokenBlacklistService;
import com.blog.Utility.PasswordHasher;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final PasswordHasher passwordHasher;
    private final JwtService jwtService;
    private final BlogUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;
    private final RefreshTokenService refreshTokenService;

    public Map<String, String> login(String username, String password) {
        User user = repository.findByUsername(username.trim()).orElseThrow(() -> new AuthenticationException("Invalid credentials"));
        if (!passwordHasher.verifyPassword(password, user.getPasswordHash())) throw new AuthenticationException("Invalid credentials");
        UserDetails userDetails = userDetailsService.loadUserByUsername(username.trim());
        String accessToken  = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        refreshTokenService.store(refreshToken, userDetails.getUsername());
        return Map.of(
            "accessToken",  accessToken,
            "refreshToken", refreshToken,
            "type",         "Bearer"
        );
    }
    public void register(RegisterUserDTO dto) {
        if (repository.findByUsername(dto.username().trim()).isPresent()) {
            throw new EntityExistsException("Username already exists: " + dto.username());
        }
        repository.save(
            dto.withPasswordHash(passwordHasher.hashPassword(dto.password())).toEntity()
        );
    }
    public Map<String, String> refresh(String refreshToken) {
        if (jwtService.isTokenExpired(refreshToken)) {
            throw new AuthenticationException("Refresh token has expired. Please log in again.");
        }
        String username = jwtService.extractUsername(refreshToken);
        if (!refreshTokenService.isValid(refreshToken, username)) {
            throw new AuthenticationException("Refresh token is invalid or has been revoked.");
        }
        if (!jwtService.isRefreshTokenValid(refreshToken, username)) {
            throw new AuthenticationException("Invalid token type for refresh.");
        }
        refreshTokenService.evict(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        String newAccess  = jwtService.generateAccessToken(userDetails);
        String newRefresh = jwtService.generateRefreshToken(userDetails);
        refreshTokenService.store(newRefresh, username);

        return Map.of(
            "accessToken",  newAccess,
            "refreshToken", newRefresh,
            "type",         "Bearer"
        );
    }
    public void logout(String accessToken, String refreshToken) {
        if (accessToken != null) tokenBlacklistService.revoke(accessToken);
        if (refreshToken != null) refreshTokenService.evict(refreshToken);
    }
}
