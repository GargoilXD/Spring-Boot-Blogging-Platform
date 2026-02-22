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
import java.util.stream.Collectors;

/**
 * Service for JWT token generation and validation.
 *
 * Tokens are signed with HMAC-SHA256 (HS256) using a secret key configured
 * in application.properties. Each token carries:
 *   - subject (username)
 *   - roles claim (list of granted authorities)
 *   - issuedAt timestamp
 *   - expiration timestamp
 *
 * DSA note: Token blacklisting (for logout) is handled by an in-memory
 * HashSet in TokenBlacklistService, giving O(1) lookup performance.
 */
@Service
public class JwtService {

    @Value("${security.jwt.secret}")
    private String secretKey;

    @Value("${security.jwt.expiration-ms:86400000}") // 24 hours default
    private long expirationMs;

    // ── Token Generation ─────────────────────────────────────────────────────

    /**
     * Generates a signed JWT for the given user.
     * Claims include username, roles, issue time, and expiration.
     */
    public String generateToken(UserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    // ── Token Validation ─────────────────────────────────────────────────────

    /** Extracts the username (subject) from the token. */
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    /** Returns true if the token is not expired and the username matches. */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /** Returns true if the token's expiration date is in the past. */
    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
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
