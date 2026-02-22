package com.blog.Security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
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
            log.info("[Security] Lockout expired for user: {}", username);
            return false;
        }

        return true;
    }
    public void recordFailure(String username) {
        int failures = attemptCount.merge(username, 1, Integer::sum);

        if (failures >= maxAttempts) {
            Instant lockUntil = Instant.now().plusSeconds(lockoutSeconds);
            lockoutUntil.put(username, lockUntil);
            log.warn("[Security] Account LOCKED after {} failures: {} — locked until {}",
                    failures, username, lockUntil);
        } else {
            log.warn("[Security] Failed login attempt {}/{} for user: {}", failures, maxAttempts, username);
        }
    }
    public void recordSuccess(String username) {
        int removed = attemptCount.getOrDefault(username, 0);
        attemptCount.remove(username);
        lockoutUntil.remove(username);
        if (removed > 0) {
            log.info("[Security] Successful login cleared {} failed attempts for user: {}", removed, username);
        }
    }
    public int getAttemptCount(String username) {
        return attemptCount.getOrDefault(username, 0);
    }
    public Instant getLockoutExpiry(String username) {
        return lockoutUntil.get(username);
    }
}
