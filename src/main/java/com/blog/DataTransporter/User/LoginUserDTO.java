package com.blog.DataTransporter.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login credentials for user authentication. Both username and password are required.")
public record LoginUserDTO(
    @Schema(description = "Username of the user. Will be trimmed before authentication.", example = "john_doe", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Username is required")
    String username,
    @Schema(description = "Password of the user. Must match the password used during registration.", example = "SecurePass123!", format = "password", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    String password
) {
    public LoginUserDTO {
        username = username.trim();
    }
}
