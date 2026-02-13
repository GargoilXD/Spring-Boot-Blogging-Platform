package com.blog.API.Response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Schema(description = "Standard error response wrapper for all API error cases. Contains error details and status information.")
public class ErrorResponse {
    @Schema(description = "HTTP status of the error response", example = "400", requiredMode = Schema.RequiredMode.REQUIRED)
    private final HttpStatus status;
    @Schema(description = "HTTP status code as integer", example = "400", requiredMode = Schema.RequiredMode.REQUIRED)
    private final Integer code;
    @Schema(description = "Human-readable error message describing what went wrong", example = "Invalid request data", requiredMode = Schema.RequiredMode.REQUIRED)
    private final String message;
    @Schema(description = "Timestamp when the error response was generated. ISO 8601 format.", example = "2023-07-25T12:00:00Z", format = "date-time", requiredMode = Schema.RequiredMode.REQUIRED)
    private final LocalDateTime timestamp;

    public ErrorResponse(HttpStatus status, String message) {
        this.status = status;
        this.code = status.value();
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
