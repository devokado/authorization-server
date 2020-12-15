package com.devokado.authServer.exceptions;

import org.springframework.http.HttpStatus;

/**
 * @author Alimodares
 * @since 2020-12-15
 */
public class NotFoundException extends RestException {
    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
