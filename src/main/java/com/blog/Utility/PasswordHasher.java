package com.blog.Utility;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PasswordHasher {
    private final Argon2 argon2;
    private final int iterations;
    private final int memory;
    private final int parallelism;

    public PasswordHasher(
            @Value("${security.argon2.iterations:3}") int iterations,
            @Value("${security.argon2.memory:65536}") int memory,
            @Value("${security.argon2.parallelism:4}") int parallelism) {
        this.argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id, 32, 32);
        this.iterations = iterations;
        this.memory = memory;
        this.parallelism = parallelism;
    }

    public String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return argon2.hash(iterations, memory, parallelism, password.toCharArray());
    }

    public boolean verifyPassword(String password, String storedHash) {
        if (password == null || storedHash == null) {
            return false;
        }
        return argon2.verify(storedHash, password.toCharArray());
    }
}