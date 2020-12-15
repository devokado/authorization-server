package com.devokado.authServer.exceptions;

import org.springframework.http.HttpStatus;

/**
 * @author Alimodares
 * @since 2020-12-15
 */
public class BadRequestException extends RestException {
    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
