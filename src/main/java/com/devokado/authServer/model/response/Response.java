package com.devokado.authServer.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response {
    private int status;
    private String title;
    private String type;
    @DateTimeFormat
    private LocalDateTime timestamp;

    public static Response create(int status, String title, HttpServletRequest request) {
        return new Response(status, title, request.getRequestURI(), LocalDateTime.now());
    }

    public static Response create(String title, HttpServletRequest request) {
        return new Response(200, title, request.getRequestURI(), LocalDateTime.now());
    }

    public static Response create(String title, String path) {
        return new Response(200, title, path, LocalDateTime.now());
    }

    public static Response create(int status, String title, String path) {
        return new Response(status, title, path, LocalDateTime.now());
    }
}
