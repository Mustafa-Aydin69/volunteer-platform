package com.imece.dto;

import jakarta.validation.constraints.NotBlank;

// Only "not blank" on purpose: format and length rules belong to registration. Repeating
// them here would reveal the rules to an attacker and lock out accounts if the rules change.
public record LoginRequest(

        @NotBlank(message = "E-posta boş olamaz")
        String email,

        @NotBlank(message = "Şifre boş olamaz")
        String password
) {
}
