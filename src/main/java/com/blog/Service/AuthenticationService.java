package com.blog.Service;

import com.blog.DataAccessor.Interface.UserDataAccessor;
import com.blog.DataTransporter.User.RegisterUserDTO;
import com.blog.Model.User;
import com.blog.Service.Exception.AuthenticationException;
import com.blog.Service.Exception.UserAlreadyExistsException;
import com.blog.Utility.PasswordHasher;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final UserDataAccessor dataAccessor;
    private final PasswordHasher passwordHasher;

    public AuthenticationService(UserDataAccessor dataAccessor, PasswordHasher passwordHasher) {
        this.dataAccessor = dataAccessor;
        this.passwordHasher = passwordHasher;
    }
    public void authenticate(String username, String password) {
        User user = dataAccessor.getByUsername(username.trim()).orElseThrow(() -> new AuthenticationException("Invalid credentials"));
        if (!passwordHasher.verifyPassword(password, user.getPasswordHash())) throw new AuthenticationException("Invalid credentials");
    }
    public void register(RegisterUserDTO dto) {
        dto.validate();
        if (dataAccessor.getByUsername(dto.username().trim()).isPresent()) throw new UserAlreadyExistsException("Username already exists: " + dto.username());
        dataAccessor.register(dto.withPasswordHash(passwordHasher.hashPassword(dto.password())));
    }
}