package com.imece.exception;

import org.springframework.http.HttpStatus;

// Thrown by services for expected business errors (409 conflict, 404 not found, ...).
// Carrying the status here keeps HTTP decisions out of every individual catch block.
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
