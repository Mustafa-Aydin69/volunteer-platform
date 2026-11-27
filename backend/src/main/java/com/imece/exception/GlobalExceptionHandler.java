package com.imece.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Translates exceptions thrown anywhere below the controllers into the { "message" } body,
// so controllers and services never build error responses themselves.
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException ex) {
        return build(ex.getStatus(), ex.getMessage());
    }

    // Raised by @Valid on request bodies; the first field error is enough for the user.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        FieldError error = ex.getBindingResult().getFieldError();
        String message = error == null
                ? "Geçersiz istek"
                : error.getField() + ": " + error.getDefaultMessage();
        return build(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "İstek gövdesi okunamadı");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex) {
        // Spring's own web exceptions (unknown URL, wrong HTTP method, ...) already know
        // their status; without this check they would all turn into 500.
        if (ex instanceof ErrorResponse springError) {
            return build(springError.getStatusCode(), springError.getBody().getDetail());
        }
        // The log stays in English for developers; only the user-facing message is translated.
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Beklenmeyen bir hata oluştu");
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatusCode status, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(message));
    }
}
