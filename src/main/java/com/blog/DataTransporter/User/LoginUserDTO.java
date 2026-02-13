package com.blog.DataTransporter.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login credentials for user authentication")
public record LoginUserDTO(
    @Schema(description = "Username of the user", example = "john doe")
    @NotBlank(message = "Username is required")
    String username,
    @Schema(description = "Password of the user", example = "password123")
    @NotBlank(message = "Password is required")
    String password
) {
    public LoginUserDTO {
        username = username.trim();
    }
}
