package com.imece.dto;

import com.imece.entity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Messages are written explicitly; the validator's defaults follow the JVM locale
// and would switch language from one machine to another.
// Size limits mirror the column lengths so invalid data is rejected with 400, not a DB error.
public record RegisterRequest(

        @NotBlank(message = "Ad soyad boş olamaz")
        @Size(max = 100, message = "Ad soyad en fazla 100 karakter olabilir")
        String fullName,

        @NotBlank(message = "E-posta boş olamaz")
        @Email(message = "E-posta geçerli değil")
        @Size(max = 150, message = "E-posta en fazla 150 karakter olabilir")
        String email,

        // 72 is BCrypt's input limit; the byte-based check for non-ASCII text is in AuthService.
        @NotBlank(message = "Şifre boş olamaz")
        @Size(min = 8, max = 72, message = "Şifre 8 ile 72 karakter arasında olmalı")
        String password,

        @NotBlank(message = "Şehir boş olamaz")
        @Size(max = 50, message = "Şehir en fazla 50 karakter olabilir")
        String city,

        @NotNull(message = "Rol seçilmeli")
        Role role,

        // Required only when role is ORGANIZATION; that cross-field rule is checked in AuthService.
        @Size(max = 150, message = "Kuruluş adı en fazla 150 karakter olabilir")
        String organizationName,

        String organizationDescription
) {
}
