package com.devokado.authServer.exceptions;

/**
 * @author Alimodares
 * @since 2020-12-14
 */
public class RestException extends RuntimeException {
    private int status = 0;

    public RestException(String message, int status) {
        super(message);
        this.status = status;
    }

    public RestException(String message) {
        super(message);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
