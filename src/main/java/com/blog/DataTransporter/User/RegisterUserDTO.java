package com.blog.DataTransporter.User;

import com.blog.Model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "User registration details. All fields are required and will be validated before account creation.")
public record RegisterUserDTO(
    @NotBlank(message = "Username is required")
    @Schema(description = "Username for the account. Must be at least 3 characters and will be trimmed.", example = "john_doe", minLength = 3, requiredMode = Schema.RequiredMode.REQUIRED)
    String username,
    @NotBlank(message = "Password is required")
    @Schema(description = "Password for the account. Must be at least 8 characters. Will be hashed before storage.", example = "SecurePass123!", minLength = 8, format = "password", requiredMode = Schema.RequiredMode.REQUIRED)
    String password,
    @NotBlank(message = "Full name is required")
    @Schema(description = "Full name of the user. Can include first and last name.", example = "John Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    String fullName,
    @NotBlank(message = "Email is required")
    @Schema(description = "Email address for the account. Must be a valid email format and will be trimmed.", example = "john.doe@example.com", format = "email", requiredMode = Schema.RequiredMode.REQUIRED)
    String email,
    @NotBlank(message = "Gender is required")
    @Schema(description = "Gender of the user. Accepted values: Male, Female, Other, M, F. Will be normalized to single character (M/F/O).", example = "Male", allowableValues = {"Male", "Female", "Other", "M", "F"}, requiredMode = Schema.RequiredMode.REQUIRED)
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
