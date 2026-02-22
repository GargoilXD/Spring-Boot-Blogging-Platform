package com.blog.Security;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {
    private final Set<String> revokedTokens = Collections.newSetFromMap(new ConcurrentHashMap<>());
    public void revoke(String token) {
        revokedTokens.add(token);
    }
    public boolean isRevoked(String token) {
        return revokedTokens.contains(token);
    }
}
