package com.blog.Security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {
    @Value("${security.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${security.login.lockout-seconds:300}")
    private long lockoutSeconds;

    private final ConcurrentHashMap<String, Integer> attemptCount = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Instant> lockoutUntil = new ConcurrentHashMap<>();

    public boolean isLockedOut(String username) {
        Instant lockExpiry = lockoutUntil.get(username);
        if (lockExpiry == null) return false;
        if (Instant.now().isAfter(lockExpiry)) {
            lockoutUntil.remove(username);
            attemptCount.remove(username);
            return false;
        }
        return true;
    }
    public void recordFailure(String username) {
        int failures = attemptCount.merge(username, 1, Integer::sum);
        if (failures >= maxAttempts) {
            lockoutUntil.put(username, Instant.now().plusSeconds(lockoutSeconds));
        }
    }
    public void recordSuccess(String username) {
        attemptCount.remove(username);
        lockoutUntil.remove(username);
    }
    public int getAttemptCount(String username) {
        return attemptCount.getOrDefault(username, 0);
    }
    public Instant getLockoutExpiry(String username) {
        return lockoutUntil.get(username);
    }
}
