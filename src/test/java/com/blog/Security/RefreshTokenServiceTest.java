package com.blog.Security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenServiceTest {
    private RefreshTokenService service;
    @BeforeEach
    void setUp() {
        service = new RefreshTokenService();
        ReflectionTestUtils.setField(service, "refreshExpirationMs", 604_800_000L); // 7 days
    }
    @Test
    @DisplayName("Stored token is valid for the correct user")
    void store_ThenIsValid_ReturnsTrue() {
        service.store("token123", "alice");
        assertTrue(service.isValid("token123", "alice"));
    }
    @Test
    @DisplayName("Stored token is invalid for a different user")
    void isValid_WrongUser_ReturnsFalse() {
        service.store("token123", "alice");
        assertFalse(service.isValid("token123", "bob"));
    }
    @Test
    @DisplayName("Evicted token is no longer valid")
    void evict_TokenBecomesInvalid() {
        service.store("token123", "alice");
        service.evict("token123");
        assertFalse(service.isValid("token123", "alice"));
    }
    @Test
    @DisplayName("Unknown token returns false from isValid")
    void isValid_UnknownToken_ReturnsFalse() {
        assertFalse(service.isValid("unknown", "alice"));
    }
    @Test
    @DisplayName("getUsernameFor returns correct username")
    void getUsernameFor_ReturnsUsername() {
        service.store("token123", "alice");
        assertEquals("alice", service.getUsernameFor("token123"));
    }
    @Test
    @DisplayName("getUsernameFor returns null for unknown token")
    void getUsernameFor_UnknownToken_ReturnsNull() {
        assertNull(service.getUsernameFor("no-such-token"));
    }
    @Test
    @DisplayName("Storing new token evicts the previous token for same user")
    void store_NewToken_EjectsPreviousToken() {
        service.store("token-old", "alice");
        service.store("token-new", "alice");
        assertFalse(service.isValid("token-old", "alice"), "Old token should be evicted");
        assertTrue(service.isValid("token-new", "alice"),  "New token should be valid");
    }
    @Test
    @DisplayName("Expired token (TTL = 0ms) is not valid")
    void expiredToken_IsNotValid() throws InterruptedException {
        ReflectionTestUtils.setField(service, "refreshExpirationMs", 0L);
        service.store("expired-token", "alice");
        Thread.sleep(100);
        assertFalse(service.isValid("expired-token", "alice"));
    }
}
