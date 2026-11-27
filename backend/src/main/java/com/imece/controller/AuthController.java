package com.imece.controller;

import com.imece.config.AuthenticatedUser;
import com.imece.dto.AuthResponse;
import com.imece.dto.LoginRequest;
import com.imece.dto.RegisterRequest;
import com.imece.dto.UserResponse;
import com.imece.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Thin HTTP layer: validates the body and delegates; all business rules live in AuthService.
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // Protected by anyRequest().authenticated(): without a valid token the request never
    // gets here, so "me" is always the principal the JWT filter put in the security context.
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AuthenticatedUser me) {
        return authService.getCurrentUser(me.id());
    }
}
