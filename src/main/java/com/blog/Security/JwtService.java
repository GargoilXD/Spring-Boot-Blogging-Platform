package com.blog.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ──────────────────────────────────────────────────────────────
 *  Epic 2 – JWT Authentication Service
 * ──────────────────────────────────────────────────────────────
 *
 *  Handles generation and validation of two token types:
 *
 *  ACCESS TOKEN
 *    - Short-lived (default 15 minutes)
 *    - Carries username + roles claim
 *    - Used on every protected API request via Authorization header
 *    - Signed with HMAC-SHA256 (HS256)
 *
 *  REFRESH TOKEN
 *    - Long-lived (default 7 days)
 *    - Carries only username (no role claims)
 *    - Used once to obtain a new access token
 *    - Stored and tracked in RefreshTokenService
 *    - Distinguished by "type": "refresh" claim
 *
 *  Both token types are standard JWTs signed with the same secret key.
 *  User Story 2.2: tokens include subject, issuedAt, expiration, and
 *  can be decoded and verified via Postman or the /token/inspect endpoint.
 */
@Service
public class JwtService {

    @Value("${security.jwt.secret}")
    private String secretKey;

    // Access token: short-lived (default 15 min → override in properties to 86400000 for dev)
    @Value("${security.jwt.expiration-ms:900000}")
    private long accessExpirationMs;

    // Refresh token: long-lived (default 7 days)
    @Value("${security.jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    // ── Access Token ──────────────────────────────────────────────────────────

    /**
     * Generates a short-lived access token containing the user's roles.
     *
     * Claims:
     *   sub   — username
     *   roles — list of ROLE_* strings (e.g. ["ROLE_AUTHOR"])
     *   type  — "access"
     *   iat   — issued at (epoch seconds)
     *   exp   — expiration (epoch seconds)
     */
    public String generateAccessToken(UserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", roles)
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Kept for backwards compatibility with Epic 1 code.
     * Delegates to generateAccessToken().
     */
    public String generateToken(UserDetails userDetails) {
        return generateAccessToken(userDetails);
    }

    // ── Refresh Token ──────────────────────────────────────────────────────────

    /**
     * Generates a long-lived refresh token.
     * Does NOT include role claims — only username and type.
     * The RefreshTokenService tracks which refresh tokens are active.
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    // ── Validation ────────────────────────────────────────────────────────────

    /** Extracts the username (subject) from any token type. */
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Validates an access token:
     *   1. Username matches the UserDetails
     *   2. Token is not expired
     *   3. Token type is "access" (not a refresh token being misused)
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        Claims claims = extractClaims(token);
        boolean usernameMatch = claims.getSubject().equals(userDetails.getUsername());
        boolean notExpired = !claims.getExpiration().before(new Date());
        boolean isAccessType = "access".equals(claims.get("type", String.class));
        return usernameMatch && notExpired && isAccessType;
    }

    /**
     * Validates a refresh token:
     *   1. Username matches
     *   2. Token is not expired
     *   3. Token type is "refresh" (not an access token being misused)
     */
    public boolean isRefreshTokenValid(String token, String username) {
        Claims claims = extractClaims(token);
        boolean usernameMatch = claims.getSubject().equals(username);
        boolean notExpired = !claims.getExpiration().before(new Date());
        boolean isRefreshType = "refresh".equals(claims.get("type", String.class));
        return usernameMatch && notExpired && isRefreshType;
    }

    /** Returns true if the token's expiration date is in the past. */
    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    /**
     * Returns a structured map of all token claims for the /token/inspect endpoint.
     * Includes human-readable timestamps (User Story 2.2).
     */
    public Map<String, Object> inspectToken(String token) {
        Claims claims = extractClaims(token);
        return Map.of(
            "subject",    claims.getSubject(),
            "roles",      claims.get("roles", List.class) != null ? claims.get("roles", List.class) : List.of(),
            "type",       claims.getOrDefault("type", "unknown"),
            "issuedAt",   claims.getIssuedAt().toString(),
            "expiration", claims.getExpiration().toString(),
            "isExpired",  claims.getExpiration().before(new Date()),
            "algorithm",  "HS256"
        );
    }

    /** Extracts all claims from the token (validates signature internally). */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
