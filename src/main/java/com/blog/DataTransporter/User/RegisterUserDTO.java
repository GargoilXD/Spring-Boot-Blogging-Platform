package com.blog.DataTransporter.User;

import jakarta.validation.constraints.NotBlank;

public record RegisterUserDTO(@NotBlank String username, @NotBlank String password, @NotBlank String fullName, @NotBlank String email, @NotBlank String gender) {
    public RegisterUserDTO withPasswordHash(String newPassword) {
        return new RegisterUserDTO(username, newPassword, fullName, email, gender);
    }
    public void validate() {
        if (username().trim().length() < 3) throw new IllegalArgumentException("Username must be at least 3 characters");
        if (password().length() < 8) throw new IllegalArgumentException("Password must be at least 8 characters");
        if (!email().trim().matches("^[A-Za-z0-9+_.-]+@(.+)$")) throw new IllegalArgumentException("Invalid email format");
    }
}
