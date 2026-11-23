package com.imece;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

// Users are authenticated with JWT against our own users table, so Spring Boot's
// generated in-memory user (and its "generated security password" log line) is not wanted.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan
public class ImeceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImeceApplication.class, args);
    }
}
