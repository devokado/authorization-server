package com.devokado.authServer.exceptions;

/**
 * @author Alimodares
 * @since 2020-12-14
 */
public class CustomException extends RuntimeException {
    private Object object;
    private int status = 0;

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public CustomException(Object object, int status) {
        this.object = object;
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
