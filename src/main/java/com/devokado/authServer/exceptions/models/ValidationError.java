package com.devokado.authServer.exceptions.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ValidationError extends SubError {
    private String object;
    private String message;

    public ValidationError(String object, String message) {
        this.object = object;
        this.message = message;
    }
}