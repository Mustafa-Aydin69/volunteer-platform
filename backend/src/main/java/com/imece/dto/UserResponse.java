package com.imece.dto;

import com.imece.entity.Organization;
import com.imece.entity.User;
import com.imece.entity.enums.OrganizationStatus;
import com.imece.entity.enums.Role;

// Entities are never returned directly: this keeps passwordHash out of every response
// and lets the entity change without breaking the API contract.
public record UserResponse(
        Long id,
        String fullName,
        String email,
        Role role,
        String city,
        // Only filled for organizations; the frontend shows the approval state from it.
        OrganizationStatus organizationStatus
) {

    public static UserResponse from(User user, Organization organization) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getCity(),
                organization == null ? null : organization.getStatus()
        );
    }
}
