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
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Value("${security.jwt.secret}")
    private String secretKey;

    @Value("${security.jwt.expiration-ms:900000}")
    private long accessExpirationMs;

    @Value("${security.jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    public String generateAccessToken(UserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", roles)
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }
    public String generateRefreshToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }
    public boolean isTokenValid(String token, UserDetails userDetails) {
        Claims claims = extractClaims(token);
        boolean usernameMatch = claims.getSubject().equals(userDetails.getUsername());
        boolean notExpired = !claims.getExpiration().before(new Date());
        boolean isAccessType = "access".equals(claims.get("type", String.class));
        return usernameMatch && notExpired && isAccessType;
    }
    public boolean isRefreshTokenValid(String token, String username) {
        Claims claims = extractClaims(token);
        boolean usernameMatch = claims.getSubject().equals(username);
        boolean notExpired = !claims.getExpiration().before(new Date());
        boolean isRefreshType = "refresh".equals(claims.get("type", String.class));
        return usernameMatch && notExpired && isRefreshType;
    }
    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }
    public Map<String, Object> inspectToken(String token) {
        Claims claims = extractClaims(token);
        return Map.of(
            "subject",    claims.getSubject(),
            "roles",      claims.get("roles", List.class) != null ? claims.get("roles", List.class) : List.of(),
            "type",       claims.getOrDefault("type", "unknown"),
            "issuedAt",   claims.getIssuedAt().toString(),
            "expiration", claims.getExpiration().toString(),
            "isExpired",  claims.getExpiration().before(new Date()),
            "algorithm",  "HS256"
        );
    }
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
