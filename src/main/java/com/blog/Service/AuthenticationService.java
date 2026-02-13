package com.blog.Service;

import com.blog.Repository.UserRepository;
import com.blog.DataTransporter.User.RegisterUserDTO;
import com.blog.Model.User;
import com.blog.Exception.AuthenticationException;
import com.blog.Utility.PasswordHasher;
import jakarta.persistence.EntityExistsException;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final UserRepository repository;
    private final PasswordHasher passwordHasher;

    public AuthenticationService(UserRepository repository, PasswordHasher passwordHasher) {
        this.repository = repository;
        this.passwordHasher = passwordHasher;
    }
    public void login(String username, String password) {
        User user = repository.findByUsername(username.trim()).orElseThrow(() -> new AuthenticationException("Invalid credentials"));
        if (!passwordHasher.verifyPassword(password, user.getPasswordHash())) throw new AuthenticationException("Invalid credentials");
    }
    public void register(RegisterUserDTO DTO) {
        if (repository.findByUsername(DTO.username().trim()).isPresent()) throw new EntityExistsException("Username already exists: " + DTO.username());
        repository.save(DTO.withPasswordHash(passwordHasher.hashPassword(DTO.password())).toEntity());
    }
}