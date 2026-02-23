package com.blog.DataTransporter.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Refresh token request body")
public record RefreshRequestDTO(
    @NotBlank(message = "Refresh token is required")
    @Schema(description = "The refresh token received at login", requiredMode = Schema.RequiredMode.REQUIRED)
    String refreshToken
) {}
