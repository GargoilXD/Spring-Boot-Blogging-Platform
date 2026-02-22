package com.blog.Security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ──────────────────────────────────────────────────────────────
 *  Epic 2 – Refresh Token Store
 * ──────────────────────────────────────────────────────────────
 *
 *  Tracks active refresh tokens in memory using two ConcurrentHashMaps:
 *
 *   tokenToUsername  — token string → username (used to look up who owns a token)
 *   tokenToExpiry    — token string → expiry Instant (used for TTL-aware validation)
 *
 *  DSA Concept — Dual HashMap:
 *    Both lookups are O(1) average case. Using two maps avoids needing a
 *    heavier data structure (e.g., a sorted tree) for TTL. On each store/
 *    validate call, expired tokens for the same user can be lazily evicted.
 *
 *  Limitations (same as TokenBlacklistService):
 *    - State lost on restart
 *    - Does not scale horizontally without a shared store (e.g., Redis)
 *
 *  Production alternative:
 *    Store refresh tokens in the database with a `refresh_tokens` table:
 *      id, user_id, token_hash, expires_at, revoked
 *    This persists across restarts and enables per-device token management.
 */
@Service
public class RefreshTokenService {

    @Value("${security.jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    // token → username
    private final ConcurrentHashMap<String, String> tokenToUsername = new ConcurrentHashMap<>();

    // token → expiry instant
    private final ConcurrentHashMap<String, Instant> tokenToExpiry = new ConcurrentHashMap<>();

    /**
     * Stores a new refresh token for the given user.
     * Automatically cleans up any previously stored token for the same user
     * to prevent unbounded growth (one active refresh token per user).
     */
    public void store(String token, String username) {
        // Evict the old token for this user (one-active-token-per-user policy)
        tokenToUsername.entrySet().removeIf(e -> e.getValue().equals(username));
        tokenToExpiry.entrySet().removeIf(e ->
            tokenToUsername.getOrDefault(e.getKey(), "").equals(username));

        tokenToUsername.put(token, username);
        tokenToExpiry.put(token, Instant.now().plusMillis(refreshExpirationMs));
    }

    /**
     * Returns true if the token is stored, maps to the given username,
     * and has not yet expired.
     */
    public boolean isValid(String token, String username) {
        String storedUsername = tokenToUsername.get(token);
        Instant expiry = tokenToExpiry.get(token);

        if (storedUsername == null || expiry == null) return false;
        if (!storedUsername.equals(username)) return false;
        if (Instant.now().isAfter(expiry)) {
            evict(token);   // lazy expiry cleanup
            return false;
        }
        return true;
    }

    /**
     * Revokes a refresh token (called on logout or after rotation).
     */
    public void evict(String token) {
        tokenToUsername.remove(token);
        tokenToExpiry.remove(token);
    }

    /**
     * Returns the username associated with the given refresh token,
     * or null if the token is unknown or expired.
     */
    public String getUsernameFor(String token) {
        return tokenToUsername.get(token);
    }
}
