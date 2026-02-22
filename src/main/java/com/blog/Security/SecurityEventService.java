package com.blog.Security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Slf4j
@Service
public class SecurityEventService {
    private static final int MAX_EVENTS = 1000;
    private final ConcurrentLinkedDeque<SecurityEvent> eventLog = new ConcurrentLinkedDeque<>();
    public void loginSuccess(String username, String ipAddress) {
        record("LOGIN_SUCCESS", username, ipAddress, "User authenticated successfully");
    }
    public void loginFailure(String username, String ipAddress, String reason) {
        record("LOGIN_FAILURE", username, ipAddress, "Login failed: " + reason);
    }
    public void accountLocked(String username, String ipAddress, int attemptCount) {
        record("ACCOUNT_LOCKED", username, ipAddress, "Account locked after " + attemptCount + " failed attempts");
    }
    public void loginBlocked(String username, String ipAddress) {
        record("LOGIN_BLOCKED", username, ipAddress, "Login rejected — account is locked out");
    }
    public void tokenIssued(String username, String tokenType) {
        record("TOKEN_ISSUED", username, "N/A", tokenType + " token issued");
    }
    public void tokenRevoked(String username, String reason) {
        record("TOKEN_REVOKED", username, "N/A", "Token revoked: " + reason);
    }
    public void tokenRefreshed(String username) {
        record("TOKEN_REFRESHED", username, "N/A", "Token pair rotated on refresh");
    }
    public void oauth2Login(String email, String provider, boolean isNew) {
        record("OAUTH2_LOGIN", email, "N/A", provider + " OAuth2 login — " + (isNew ? "new user registered" : "existing user"));
    }
    public void unauthorizedAccess(String username, String resource) {
        record("UNAUTHORIZED_ACCESS", username, "N/A", "Access denied to resource: " + resource);
    }
    public List<SecurityEvent> getAllEvents() {
        return List.copyOf(eventLog);
    }
    public List<SecurityEvent> getRecentEvents(int n) {
        List<SecurityEvent> all = new ArrayList<>(eventLog);
        int start = Math.max(0, all.size() - n);
        return Collections.unmodifiableList(all.subList(start, all.size()));
    }
    public List<SecurityEvent> getEventsByType(String type) {
        return eventLog.stream().filter(e -> e.type().equalsIgnoreCase(type)).toList();
    }
    private void record(String type, String username, String ip, String detail) {
        SecurityEvent event = new SecurityEvent(type, username, ip, detail, Instant.now());
        if (eventLog.size() >= MAX_EVENTS) {
            eventLog.pollFirst();
        }
        eventLog.addLast(event);
        log.info("[SecurityEvent] type={} user={} ip={} detail={}", type, username, ip, detail);
    }
}
