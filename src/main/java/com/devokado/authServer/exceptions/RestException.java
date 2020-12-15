package com.devokado.authServer.exceptions;

import org.springframework.http.HttpStatus;

/**
 * @author Alimodares
 * @since 2020-12-14
 */
public class RestException extends RuntimeException {
    private HttpStatus status;

    public RestException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }
}
