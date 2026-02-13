package com.blog.API.Response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Schema(description = "Standard success response wrapper for all API endpoints. Contains status information and optional data payload.")
public class SuccessResponse<T> {
    @Schema(description = "HTTP status of the response", example = "200", requiredMode = Schema.RequiredMode.REQUIRED)
    private final HttpStatus status;
    @Schema(description = "HTTP status code as integer", example = "200", requiredMode = Schema.RequiredMode.REQUIRED)
    private final Integer code;
    @Schema(description = "Human-readable success message", example = "Operation completed successfully", requiredMode = Schema.RequiredMode.REQUIRED)
    private final String message;
    @Schema(description = "Response data payload. Can be any type depending on the endpoint. May be null for operations with no return data.")
    private final T data;
    @Schema(description = "Timestamp when the response was generated. ISO 8601 format.", example = "2023-07-25T12:00:00Z", format = "date-time", requiredMode = Schema.RequiredMode.REQUIRED)
    private final LocalDateTime timestamp;

    public SuccessResponse(HttpStatus status, String message, T data) {
        this.status = status;
        this.code = status.value();
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
    public SuccessResponse(HttpStatus status, String message) {
        this.status = status;
        this.code = status.value();
        this.message = message;
        this.data = null;
        this.timestamp = LocalDateTime.now();
    }
}