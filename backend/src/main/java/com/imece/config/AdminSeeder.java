package com.imece.config;

import com.imece.entity.User;
import com.imece.entity.enums.Role;
import com.imece.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

// Admins cannot register through the API, so the first one is created here from
// environment variables. Credentials never enter the repository.
@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private static final String ADMIN_FULL_NAME = "Admin";
    // users.city is mandatory, but no feature uses the admin's city.
    private static final String ADMIN_CITY = "İstanbul";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;

    public AdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AdminProperties adminProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminProperties = adminProperties;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByRole(Role.ADMIN)) {
            return;
        }
        // Fail fast: a platform without an admin cannot approve any organization.
        if (!StringUtils.hasText(adminProperties.email()) || !StringUtils.hasText(adminProperties.password())) {
            throw new IllegalStateException(
                    "No admin account exists. Set ADMIN_EMAIL and ADMIN_PASSWORD to create one.");
        }
        User admin = new User(ADMIN_FULL_NAME, adminProperties.email(),
                passwordEncoder.encode(adminProperties.password()), Role.ADMIN, ADMIN_CITY);
        userRepository.save(admin);
        log.info("Admin account created for {}", adminProperties.email());
    }
}
