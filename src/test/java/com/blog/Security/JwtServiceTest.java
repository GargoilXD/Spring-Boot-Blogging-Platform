package com.blog.Security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private JwtService jwtService;
    private static final String TEST_SECRET = "MyPftS4ocQf396p50zpOKkcNmtm57qnxyZAcTPlmClo";

    private UserDetails authorUser;
    private UserDetails adminUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey",          TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "accessExpirationMs", 900_000L);    // 15 min
        ReflectionTestUtils.setField(jwtService, "refreshExpirationMs", 604_800_000L); // 7 days

        authorUser = User.withUsername("author1")
                .password("hashed")
                .authorities(new SimpleGrantedAuthority("ROLE_AUTHOR"))
                .build();

        adminUser = User.withUsername("admin1")
                .password("hashed")
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                .build();
    }

    @Nested
    @DisplayName("Access Token — Generation")
    class AccessTokenGeneration {

        @Test
        @DisplayName("Generated token is not null or blank")
        void generateAccessToken_NotNull() {
            String token = jwtService.generateAccessToken(authorUser);
            assertNotNull(token);
            assertFalse(token.isBlank());
        }

        @Test
        @DisplayName("Token has three JWT parts (header.payload.signature)")
        void generateAccessToken_ThreeParts() {
            String token = jwtService.generateAccessToken(authorUser);
            String[] parts = token.split("\\.");
            assertEquals(3, parts.length, "JWT must have exactly 3 dot-separated parts");
        }

        @Test
        @DisplayName("Subject claim equals username")
        void generateAccessToken_SubjectIsUsername() {
            String token = jwtService.generateAccessToken(authorUser);
            assertEquals("author1", jwtService.extractUsername(token));
        }

        @Test
        @DisplayName("Roles claim contains user's authority")
        void generateAccessToken_ContainsRoles() {
            String token = jwtService.generateAccessToken(authorUser);
            Map<String, Object> claims = jwtService.inspectToken(token);
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) claims.get("roles");
            assertTrue(roles.contains("ROLE_AUTHOR"));
        }

        @Test
        @DisplayName("Type claim is 'access'")
        void generateAccessToken_TypeIsAccess() {
            String token = jwtService.generateAccessToken(authorUser);
            Map<String, Object> claims = jwtService.inspectToken(token);
            assertEquals("access", claims.get("type"));
        }

        @Test
        @DisplayName("Token is not expired immediately after creation")
        void generateAccessToken_NotExpiredImmediately() {
            String token = jwtService.generateAccessToken(authorUser);
            assertFalse(jwtService.isTokenExpired(token));
        }

        @Test
        @DisplayName("Admin token carries ROLE_ADMIN claim")
        void generateAccessToken_AdminRoleClaim() {
            String token = jwtService.generateAccessToken(adminUser);
            Map<String, Object> claims = jwtService.inspectToken(token);
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) claims.get("roles");
            assertTrue(roles.contains("ROLE_ADMIN"));
        }
    }

    @Nested
    @DisplayName("Refresh Token — Generation")
    class RefreshTokenGeneration {

        @Test
        @DisplayName("Refresh token is not null or blank")
        void generateRefreshToken_NotNull() {
            String token = jwtService.generateRefreshToken(authorUser);
            assertNotNull(token);
            assertFalse(token.isBlank());
        }

        @Test
        @DisplayName("Refresh token type claim is 'refresh'")
        void generateRefreshToken_TypeIsRefresh() {
            String token = jwtService.generateRefreshToken(authorUser);
            Map<String, Object> claims = jwtService.inspectToken(token);
            assertEquals("refresh", claims.get("type"));
        }

        @Test
        @DisplayName("Refresh token subject equals username")
        void generateRefreshToken_SubjectIsUsername() {
            String token = jwtService.generateRefreshToken(authorUser);
            assertEquals("author1", jwtService.extractUsername(token));
        }

        @Test
        @DisplayName("Access and refresh tokens for same user are different strings")
        void accessAndRefresh_AreDifferent() {
            String access  = jwtService.generateAccessToken(authorUser);
            String refresh = jwtService.generateRefreshToken(authorUser);
            assertNotEquals(access, refresh);
        }

        @Test
        @DisplayName("isRefreshTokenValid returns true for valid refresh token")
        void isRefreshTokenValid_True() {
            String token = jwtService.generateRefreshToken(authorUser);
            assertTrue(jwtService.isRefreshTokenValid(token, "author1"));
        }

        @Test
        @DisplayName("isRefreshTokenValid returns false for access token")
        void isRefreshTokenValid_FalseForAccessToken() {
            String accessToken = jwtService.generateAccessToken(authorUser);
            assertFalse(jwtService.isRefreshTokenValid(accessToken, "author1"));
        }
    }

    @Nested
    @DisplayName("Token Validation")
    class TokenValidation {

        @Test
        @DisplayName("isTokenValid returns true for fresh access token")
        void isTokenValid_True() {
            String token = jwtService.generateAccessToken(authorUser);
            assertTrue(jwtService.isTokenValid(token, authorUser));
        }

        @Test
        @DisplayName("isTokenValid returns false for wrong username")
        void isTokenValid_FalseForWrongUser() {
            String token = jwtService.generateAccessToken(authorUser);
            assertFalse(jwtService.isTokenValid(token, adminUser));
        }

        @Test
        @DisplayName("isTokenValid returns false for refresh token (type mismatch)")
        void isTokenValid_FalseForRefreshToken() {
            String refresh = jwtService.generateRefreshToken(authorUser);
            assertFalse(jwtService.isTokenValid(refresh, authorUser));
        }

        @Test
        @DisplayName("Tampered token is rejected")
        void tamperedToken_IsRejected() {
            String token = jwtService.generateAccessToken(authorUser);
            String tampered = token.substring(0, token.length() - 5) + "XXXXX";
            assertThrows(Exception.class, () -> jwtService.extractClaims(tampered));
        }

        @Test
        @DisplayName("Expired token fails isTokenExpired check")
        void expiredToken_IsDetected() {
            ReflectionTestUtils.setField(jwtService, "accessExpirationMs", -1L);
            String expiredToken = jwtService.generateAccessToken(authorUser);
            assertTrue(jwtService.isTokenExpired(expiredToken));
        }
    }

    @Nested
    @DisplayName("Token Inspection (User Story 2.2)")
    class TokenInspection {

        @Test
        @DisplayName("inspectToken returns all required claim keys")
        void inspectToken_ReturnsAllKeys() {
            String token = jwtService.generateAccessToken(authorUser);
            Map<String, Object> claims = jwtService.inspectToken(token);

            assertAll(
                () -> assertTrue(claims.containsKey("subject")),
                () -> assertTrue(claims.containsKey("roles")),
                () -> assertTrue(claims.containsKey("type")),
                () -> assertTrue(claims.containsKey("issuedAt")),
                () -> assertTrue(claims.containsKey("expiration")),
                () -> assertTrue(claims.containsKey("isExpired")),
                () -> assertTrue(claims.containsKey("algorithm"))
            );
        }

        @Test
        @DisplayName("inspectToken reports algorithm as HS256")
        void inspectToken_AlgorithmIsHS256() {
            String token = jwtService.generateAccessToken(authorUser);
            Map<String, Object> claims = jwtService.inspectToken(token);
            assertEquals("HS256", claims.get("algorithm"));
        }

        @Test
        @DisplayName("inspectToken reports isExpired false for fresh token")
        void inspectToken_IsExpiredFalse() {
            String token = jwtService.generateAccessToken(authorUser);
            Map<String, Object> claims = jwtService.inspectToken(token);
            assertFalse((Boolean) claims.get("isExpired"));
        }

        @Test
        @DisplayName("generateAccessToken() delegates to generateToken()")
        void generateAccessToken_DelegatesToAccessToken() {
            String token = jwtService.generateAccessToken(authorUser);
            Map<String, Object> claims = jwtService.inspectToken(token);
            assertEquals("access", claims.get("type"));
        }
    }
}
