package com.blog.Security;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory token blacklist for handling JWT logout/revocation.
 *
 * DSA Concept — HashMap / HashSet:
 *   Uses a thread-safe ConcurrentHashMap-backed Set for O(1) average-case
 *   lookup when checking if a token is revoked. This avoids scanning a list
 *   on every authenticated request.
 *
 * Limitations of in-memory approach:
 *   - Revoked tokens are lost on server restart
 *   - Does not scale horizontally (each node has its own blacklist)
 *   - No TTL — tokens stay in memory even after natural expiry
 *
 * Production alternative: Redis with per-token TTL aligned to JWT expiration.
 */
@Service
public class TokenBlacklistService {

    /**
     * Thread-safe set of revoked JWT strings.
     * ConcurrentHashMap.newKeySet() gives O(1) add/contains with no lock contention.
     */
    private final Set<String> revokedTokens = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /** Adds a token to the blacklist (called on logout). */
    public void revoke(String token) {
        revokedTokens.add(token);
    }

    /** Returns true if the token has been explicitly revoked. */
    public boolean isRevoked(String token) {
        return revokedTokens.contains(token);
    }
}
