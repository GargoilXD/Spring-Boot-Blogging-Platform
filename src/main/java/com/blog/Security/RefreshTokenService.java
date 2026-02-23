package com.blog.Security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RefreshTokenService {
    @Value("${security.jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;
    private final ConcurrentHashMap<String, String> tokenToUsername = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Instant> tokenToExpiry = new ConcurrentHashMap<>();

    public void store(String token, String username) {
        tokenToUsername.entrySet().removeIf(e -> e.getValue().equals(username));
        tokenToExpiry.entrySet().removeIf(e -> tokenToUsername.getOrDefault(e.getKey(), "").equals(username));
        tokenToUsername.put(token, username);
        tokenToExpiry.put(token, Instant.now().plusMillis(refreshExpirationMs));
    }
    public boolean isValid(String token, String username) {
        String storedUsername = tokenToUsername.get(token);
        Instant expiry = tokenToExpiry.get(token);
        if (storedUsername == null || expiry == null) return false;
        if (!storedUsername.equals(username)) return false;
        if (Instant.now().isAfter(expiry)) {
            evict(token);
            return false;
        }
        return true;
    }
    public void evict(String token) {
        tokenToUsername.remove(token);
        tokenToExpiry.remove(token);
    }
    public String getUsernameFor(String token) {
        return tokenToUsername.get(token);
    }
}
