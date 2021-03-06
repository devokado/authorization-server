package com.devokado.authServer.exceptions;

import org.springframework.http.HttpStatus;

/**
 * @author Alimodares
 * @since 2020-12-15
 */
public class NotAvailableException extends RestException {
    public NotAvailableException(String message) {
        super(message, HttpStatus.GONE);
    }
}
