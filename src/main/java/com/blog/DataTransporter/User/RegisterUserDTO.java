package com.blog.DataTransporter.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "User registration details")
public record RegisterUserDTO(
    @NotBlank
    @Schema(description = "Username (minimum 3 characters)", example = "johndoe", required = true, minLength = 3)
    String username,
    @NotBlank
    @Schema(description = "Password (minimum 8 characters)", example = "SecurePass123!", required = true, minLength = 8)
    String password,
    @NotBlank
    @Schema(description = "Full name of the user", example = "John Doe", required = true)
    String fullName,
    @NotBlank
    @Schema(description = "Email address", example = "john.doe@example.com", required = true)
    String email,
    @NotBlank
    @Schema(description = "Gender of the user", example = "Male", required = true)
    String gender
) {
    public RegisterUserDTO withPasswordHash(String newPassword) {
        return new RegisterUserDTO(username, newPassword, fullName, email, gender);
    }
    public void validate() {
        if (username().trim().length() < 3) throw new IllegalArgumentException("Username must be at least 3 characters");
        if (password().length() < 8) throw new IllegalArgumentException("Password must be at least 8 characters");
        if (!email().trim().matches("^[A-Za-z0-9+_.-]+@(.+)$")) throw new IllegalArgumentException("Invalid email format");
    }
}
