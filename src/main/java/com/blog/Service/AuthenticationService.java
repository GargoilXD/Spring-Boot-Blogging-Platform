package com.blog.Service;

import com.blog.Repository.UserRepository;
import com.blog.DataTransporter.User.RegisterUserDTO;
import com.blog.Model.User;
import com.blog.Exception.AuthenticationException;
import com.blog.Exception.UserAlreadyExistsException;
import com.blog.Utility.PasswordHasher;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final UserRepository repository;
    private final PasswordHasher passwordHasher;

    public AuthenticationService(UserRepository repository, PasswordHasher passwordHasher) {
        this.repository = repository;
        this.passwordHasher = passwordHasher;
    }
    public void authenticate(String username, String password) {
        User user = repository.findByUsername(username.trim()).orElseThrow(() -> new AuthenticationException("Invalid credentials"));
        if (!passwordHasher.verifyPassword(password, user.getPasswordHash())) throw new AuthenticationException("Invalid credentials");
    }
    public void register(RegisterUserDTO dto) {
        dto.validate();
        if (repository.findByUsername(dto.username().trim()).isPresent()) throw new UserAlreadyExistsException("Username already exists: " + dto.username());
        repository.save(dto.withPasswordHash(passwordHasher.hashPassword(dto.password())).toEntity());
    }
}