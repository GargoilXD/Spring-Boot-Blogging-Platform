package com.blog.API.Response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
public class SuccessResponse<T> {
    private final HttpStatus status;
    private final Integer code;
    private final String message;
    private final T data;
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