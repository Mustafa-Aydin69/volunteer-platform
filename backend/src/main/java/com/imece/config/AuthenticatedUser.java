package com.imece.config;

import com.imece.entity.enums.Role;

// What the JWT filter stores as the logged-in principal. Controllers read it with
// @AuthenticationPrincipal to run ownership checks without another database query.
public record AuthenticatedUser(Long id, Role role) {
}
