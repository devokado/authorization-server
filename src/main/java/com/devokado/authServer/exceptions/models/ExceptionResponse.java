package com.devokado.authServer.exceptions.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExceptionResponse {
    private int status;
    private String title;
    private LocalDateTime timestamp;

    public static ExceptionResponse create(int status, String title) {
        return new ExceptionResponse(status, title, LocalDateTime.now());
    }
    public static ExceptionResponse create(String title) {
        return new ExceptionResponse(200, title, LocalDateTime.now());
    }
}
