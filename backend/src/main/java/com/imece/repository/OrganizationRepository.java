package com.imece.repository;

import com.imece.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    // "UserId" is resolved through the user relation (organization.user.id), so no join is written by hand.
    Optional<Organization> findByUserId(Long userId);
}
