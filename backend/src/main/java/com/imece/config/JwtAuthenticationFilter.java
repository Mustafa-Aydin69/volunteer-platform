package com.imece.config;

import com.imece.entity.User;
import com.imece.repository.UserRepository;
import com.imece.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// Not a @Component on purpose: Spring Boot would then also register it as a plain servlet
// filter and it would run twice. SecurityConfig creates it and places it in the chain.
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            // The user is reloaded on every request instead of trusting the token alone,
            // so a deactivated account loses access immediately, not when the token expires.
            jwtService.extractUserId(header.substring(BEARER_PREFIX.length()))
                    .flatMap(userRepository::findById)
                    .filter(User::isEnabled)
                    .ifPresent(this::authenticate);
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(User user) {
        AuthenticatedUser principal = new AuthenticatedUser(user.getId(), user.getRole());
        // The "ROLE_" prefix is what hasRole("ADMIN") in SecurityConfig looks for.
        var authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
        var authentication = new UsernamePasswordAuthenticationToken(principal, null, List.of(authority));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
