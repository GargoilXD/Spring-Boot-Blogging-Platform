package com.blog.API.Response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {
    private final HttpStatus status;
    private final Integer code;
    private final String message;
    private final LocalDateTime timestamp;

    public ErrorResponse(HttpStatus status, String message) {
        this.status = status;
        this.code = status.value();
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
