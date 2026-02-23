package com.blog.Security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SecurityEventLogger {
    private static final int BRUTE_FORCE_THRESHOLD = 5;
    private final Map<String, AtomicInteger> failureCountByUser = new ConcurrentHashMap<>();

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        Authentication auth = event.getAuthentication();
        String username = resolveUsername(auth);
        String roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", "));
        failureCountByUser.remove(username);

        log.info("[SECURITY] LOGIN SUCCESS | user={} | roles=[{}] | time={} | type={}",
                username,
                roles,
                Instant.now(),
                auth.getClass().getSimpleName());
    }
    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        String username = event.getAuthentication().getName();
        String reason   = event.getException().getMessage();
        AtomicInteger count = failureCountByUser.computeIfAbsent(username, k -> new AtomicInteger(0));
        int failures = count.incrementAndGet();
        if (failures >= BRUTE_FORCE_THRESHOLD) {
            log.error("[SECURITY] BRUTE-FORCE DETECTED | user={} | failures={} | reason={} | time={}", username, failures, reason, Instant.now());
        } else {
            log.warn("[SECURITY] LOGIN FAILED | user={} | attempt={}/{} | reason={} | time={}", username, failures, BRUTE_FORCE_THRESHOLD, reason, Instant.now());
        }
    }
    @EventListener
    public void onLogout(LogoutSuccessEvent event) {
        String username = event.getAuthentication().getName();
        log.info("[SECURITY] LOGOUT | user={} | time={}", username, Instant.now());
    }
    private String resolveUsername(Authentication auth) {
        if (auth.getPrincipal() instanceof OAuth2User oAuth2User) {
            String email = oAuth2User.getAttribute("email");
            return email != null ? email : auth.getName();
        }
        return auth.getName();
    }
    public int getFailureCount(String username) {
        AtomicInteger count = failureCountByUser.get(username);
        return count != null ? count.get() : 0;
    }
    public Map<String, Integer> getFailureSummary() {
        Map<String, Integer> summary = new ConcurrentHashMap<>();
        failureCountByUser.forEach((user, count) -> summary.put(user, count.get()));
        return summary;
    }
}
