package com.blog.Service;

import com.blog.DataAccessor.Exception.DataAccessException;
import com.blog.DataAccessor.Interface.UserDataAccessor;
import com.blog.DataTransporter.UserDataTransporter;
import com.blog.Model.User;
import com.blog.Service.Exception.AuthenticationException;
import com.blog.Service.Exception.UserAlreadyExistsException;
import com.blog.Utility.PasswordHasher;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationService {
    private final UserDataAccessor dataAccessor;
    private final PasswordHasher passwordHasher;

    public AuthenticationService(UserDataAccessor dataAccessor, PasswordHasher passwordHasher) {
        this.dataAccessor = dataAccessor;
        this.passwordHasher = passwordHasher;
    }
    public User authenticate(String username, String password)
            throws AuthenticationException, DataAccessException {

        if (username == null || username.trim().isEmpty()) {
            throw new AuthenticationException("Username is required");
        }
        if (password == null || password.isEmpty()) {
            throw new AuthenticationException("Password is required");
        }

        String cleanUsername = username.trim();
        Optional<User> userOptional = dataAccessor.getByUsername(cleanUsername);

        if (userOptional.isEmpty()) {
            throw new AuthenticationException("Invalid credentials");
        }

        User user = userOptional.get();

        if (!passwordHasher.verifyPassword(password, user.getPasswordHash())) {
            throw new AuthenticationException("Invalid credentials");
        }

        return user;
    }
    public boolean validateCredentials(String username, String password) {
        try {
            authenticate(username, password);
            return true;
        } catch (AuthenticationException | DataAccessException e) {
            return false;
        }
    }
    public void register(UserDataTransporter udto) throws IllegalArgumentException, DataAccessException, UserAlreadyExistsException {

        validateRegistrationInput(udto);

        if (dataAccessor.getByUsername(udto.username().trim()).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists: " + udto.username());
        }

        User user = new User(
                null,
                udto.username().trim(),
                passwordHasher.hashPassword(udto.passwordHash()),
                udto.fullName(),
                udto.email(),
                udto.gender(),
                null
        );
        dataAccessor.register(UserDataTransporter.fromEntity(user));
    }

    private void validateRegistrationInput(UserDataTransporter udto) {
        if (udto == null) {
            throw new IllegalArgumentException("Registration data cannot be null");
        }
        if (udto.username() == null || udto.username().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (udto.username().trim().length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters");
        }
        if (udto.passwordHash() == null || udto.passwordHash().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (udto.passwordHash().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        if (udto.email() != null && !udto.email().trim().isEmpty()) {
            String emailPattern = "^[A-Za-z0-9+_.-]+@(.+)$";
            if (!udto.email().trim().matches(emailPattern)) {
                throw new IllegalArgumentException("Invalid email format");
            }
        }
    }
}