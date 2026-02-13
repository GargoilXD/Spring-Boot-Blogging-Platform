package com.blog.DataTransporter.User;

import com.blog.Model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "User registration details")
public record RegisterUserDTO(
    @NotBlank(message = "Username is required")
    @Schema(description = "Username (minimum 3 characters)", example = "john doe", minLength = 3)
    String username,
    @NotBlank(message = "Password is required")
    @Schema(description = "Password (minimum 8 characters)", example = "SecurePass123!", minLength = 8)
    String password,
    @NotBlank(message = "Full name is required")
    @Schema(description = "Full name of the user", example = "John Doe")
    String fullName,
    @NotBlank(message = "Email is required")
    @Schema(description = "Email address", example = "john.doe@example.com")
    String email,
    @NotBlank(message = "Gender is required")
    @Schema(description = "Gender of the user", example = "Male")
    String gender
) {
    public RegisterUserDTO {
        username = username.trim();
        email = email.trim();
        gender = switch (gender.trim().toUpperCase()) {
            case "MALE", "FEMALE", "OTHER" -> String.valueOf(gender.charAt(0)).toUpperCase();
            case "F", "M" -> gender.trim().toUpperCase();
            default -> throw new IllegalArgumentException("Invalid gender");
        };
        if (username.length() < 3) throw new IllegalArgumentException("Username must be at least 3 characters");
        if (password.length() < 8) throw new IllegalArgumentException("Password must be at least 8 characters");
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) throw new IllegalArgumentException("Invalid email format");
    }

    public User toEntity() {
        return new User(null, username, password, fullName, email, gender, null);
    }
    public RegisterUserDTO withPasswordHash(String newPassword) {
        return new RegisterUserDTO(username, newPassword, fullName, email, gender);
    }
}
