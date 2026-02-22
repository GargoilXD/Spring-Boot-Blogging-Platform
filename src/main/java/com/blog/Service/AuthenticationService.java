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

/**
 * ──────────────────────────────────────────────────────────────
 *  Epic 2 – Authentication Service (JWT + Refresh Tokens)
 * ──────────────────────────────────────────────────────────────
 *
 *  login    → verify Argon2 hash → issue access + refresh tokens
 *  register → validate + hash password → save with READER role
 *  refresh  → validate refresh token → rotate tokens (new access + new refresh)
 *  logout   → blacklist access token + evict refresh token
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordHasher passwordHasher;
    private final JwtService jwtService;
    private final BlogUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;
    private final RefreshTokenService refreshTokenService;

    // ── Login ─────────────────────────────────────────────────────────────────

    /**
     * Authenticates the user and returns both an access and refresh token.
     *
     * Token rotation on login: any previously stored refresh token for this
     * user is evicted by RefreshTokenService.store() before issuing new ones.
     *
     * @return Map with "accessToken" and "refreshToken" keys
     * @throws AuthenticationException on bad credentials
     */
    public Map<String, String> login(String username, String password) {
        User user = repository.findByUsername(username.trim())
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        if (!passwordHasher.verifyPassword(password, user.getPasswordHash())) {
            throw new AuthenticationException("Invalid credentials");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username.trim());

        String accessToken  = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Store the refresh token so it can be validated and revoked later
        refreshTokenService.store(refreshToken, userDetails.getUsername());

        return Map.of(
            "accessToken",  accessToken,
            "refreshToken", refreshToken,
            "type",         "Bearer"
        );
    }

    // ── Register ──────────────────────────────────────────────────────────────

    /**
     * Creates a new user with an Argon2id-hashed password and READER role.
     *
     * @throws EntityExistsException if the username is already taken
     */
    public void register(RegisterUserDTO dto) {
        if (repository.findByUsername(dto.username().trim()).isPresent()) {
            throw new EntityExistsException("Username already exists: " + dto.username());
        }
        repository.save(
            dto.withPasswordHash(passwordHasher.hashPassword(dto.password())).toEntity()
        );
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    /**
     * Exchanges a valid refresh token for a new access + refresh token pair.
     *
     * Token rotation: the old refresh token is immediately evicted and a new
     * one is issued, limiting the window for refresh token theft.
     *
     * @param refreshToken the refresh token from the client
     * @return Map with "accessToken" and "refreshToken" keys
     * @throws AuthenticationException if the refresh token is invalid or expired
     */
    public Map<String, String> refresh(String refreshToken) {
        // 1. Verify the JWT signature and expiration
        if (jwtService.isTokenExpired(refreshToken)) {
            throw new AuthenticationException("Refresh token has expired. Please log in again.");
        }

        String username = jwtService.extractUsername(refreshToken);

        // 2. Verify the token is in our active store (not evicted/rotated)
        if (!refreshTokenService.isValid(refreshToken, username)) {
            throw new AuthenticationException("Refresh token is invalid or has been revoked.");
        }

        // 3. Verify it is actually a refresh-type token (not an access token)
        if (!jwtService.isRefreshTokenValid(refreshToken, username)) {
            throw new AuthenticationException("Invalid token type for refresh.");
        }

        // 4. Rotate: evict old token and issue new pair
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

    // ── Logout ────────────────────────────────────────────────────────────────

    /**
     * Revokes both the access and refresh tokens for the user.
     *
     * @param accessToken  the active access token (to blacklist)
     * @param refreshToken the refresh token to evict (optional — may be null)
     */
    public void logout(String accessToken, String refreshToken) {
        // Blacklist the access token so the JwtAuthFilter rejects it immediately
        if (accessToken != null) {
            tokenBlacklistService.revoke(accessToken);
        }
        // Evict the refresh token so it cannot be used to obtain a new access token
        if (refreshToken != null) {
            refreshTokenService.evict(refreshToken);
        }
    }
}
