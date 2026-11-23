package com.imece.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

// Binds the app.jwt.* block of application.yml; "24h" is converted to a Duration by Spring.
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, Duration expiration) {
}
