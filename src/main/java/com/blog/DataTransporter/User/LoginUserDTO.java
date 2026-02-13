package com.blog.DataTransporter.User;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Login credentials for user authentication")
public record LoginUserDTO(
    @Schema(description = "Username of the user", example = "john doe")
    String username,
    @Schema(description = "Password of the user", example = "password123")
    String password
) {
}
