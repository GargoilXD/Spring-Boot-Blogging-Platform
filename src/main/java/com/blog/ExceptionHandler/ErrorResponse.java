package com.blog.ExceptionHandler;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {
    private final String code;
    private final Object message;
    private final LocalDateTime timestamp;

    public ErrorResponse(String code, Object message) {
        this.code = code;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

}
