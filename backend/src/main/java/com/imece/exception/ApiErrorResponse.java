package com.imece.exception;

// Single error body shape for the whole API: { "message": "..." }.
public record ApiErrorResponse(String message) {
}
