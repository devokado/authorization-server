package com.devokado.authServer.exceptions;

import org.springframework.http.HttpStatus;

/**
 * @author Alimodares
 * @since 2020-12-15
 */
public class DuplicateException extends RestException {
    public DuplicateException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
