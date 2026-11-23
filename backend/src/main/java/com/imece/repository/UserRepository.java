package com.imece.repository;

import com.imece.entity.User;
import com.imece.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Spring Data derives the SQL from the method names (findByEmail -> WHERE email = ?).
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRole(Role role);
}
