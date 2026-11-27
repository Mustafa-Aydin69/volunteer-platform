package com.imece.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imece.exception.ApiErrorResponse;
import com.imece.repository.UserRepository;
import com.imece.service.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService,
                                                   UserRepository userRepository,
                                                   ObjectMapper objectMapper) throws Exception {
        http
                // CSRF protects cookie-based sessions; a Bearer token is never sent automatically
                // by the browser, so the attack it prevents does not apply here.
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // First layer of authorization: the URL prefix decides the role.
                // Ownership (is this record yours?) is the second layer, checked in the services.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/projects", "/api/projects/**",
                                "/api/categories", "/api/stats", "/certificates/**").permitAll()
                        // Spring forwards errors here; blocking it would hide real errors behind 401.
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/org/**").hasRole("ORGANIZATION")
                        .requestMatchers("/api/volunteer/**").hasRole("VOLUNTEER")
                        .anyRequest().authenticated())
                // These two run before any controller, so @RestControllerAdvice cannot format them.
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((request, response, ex) ->
                                writeError(response, objectMapper, HttpServletResponse.SC_UNAUTHORIZED,
                                        "Giriş yapmanız gerekiyor"))
                        .accessDeniedHandler((request, response, ex) ->
                                writeError(response, objectMapper, HttpServletResponse.SC_FORBIDDEN,
                                        "Bu işlem için yetkiniz yok")))
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, userRepository),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // The React dev server runs on another port, so the browser needs explicit permission
    // to call this API from there.
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins}") List<String> allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    private static void writeError(HttpServletResponse response, ObjectMapper objectMapper,
                                   int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), new ApiErrorResponse(message));
    }
}
