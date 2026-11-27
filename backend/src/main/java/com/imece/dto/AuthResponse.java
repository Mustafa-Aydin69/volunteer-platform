package com.imece.dto;

// Returned by register and login, so the frontend can store the token and route to the
// right panel without an extra /me request.
public record AuthResponse(String token, UserResponse user) {
}
