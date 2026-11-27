package com.imece.service;

import com.imece.dto.AuthResponse;
import com.imece.dto.RegisterRequest;
import com.imece.dto.UserResponse;
import com.imece.entity.Organization;
import com.imece.entity.User;
import com.imece.entity.enums.Role;
import com.imece.exception.ApiException;
import com.imece.repository.OrganizationRepository;
import com.imece.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class AuthService {

    // BCrypt only reads the first 72 bytes, and Spring Security rejects longer input.
    // Turkish letters take 2 bytes in UTF-8, so 72 characters can exceed this limit.
    private static final int BCRYPT_MAX_BYTES = 72;

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, OrganizationRepository organizationRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // One transaction: if saving the organization fails, the user row is rolled back too,
    // so an ORGANIZATION account can never exist without its organization.
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        validateRegistration(request);

        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Bu e-posta zaten kayıtlı");
        }

        User user = userRepository.save(new User(
                request.fullName().trim(),
                email,
                passwordEncoder.encode(request.password()),
                request.role(),
                request.city().trim()));

        Organization organization = null;
        if (request.role() == Role.ORGANIZATION) {
            organization = organizationRepository.save(new Organization(
                    user, request.organizationName().trim(), request.organizationDescription()));
        }

        return new AuthResponse(jwtService.generateToken(user), UserResponse.from(user, organization));
    }

    // Rules that @Valid on the DTO cannot express: they depend on another field or on bytes.
    private void validateRegistration(RegisterRequest request) {
        if (request.role() == Role.ADMIN) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Yönetici hesabı kayıtla oluşturulamaz");
        }
        if (request.role() == Role.ORGANIZATION && !StringUtils.hasText(request.organizationName())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Kuruluş adı zorunludur");
        }
        if (request.password().getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Şifre çok uzun");
        }
    }

    // "Ali@Mail.com " and "ali@mail.com" must be the same account.
    // Locale.ROOT avoids the Turkish dotless-i rule ("I" -> "ı") breaking addresses.
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
