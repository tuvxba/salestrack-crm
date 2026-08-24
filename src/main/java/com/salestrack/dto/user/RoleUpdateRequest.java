package com.salestrack.dto.user;

import com.salestrack.enums.Role;

import jakarta.validation.constraints.NotNull;

public record RoleUpdateRequest(
        @NotNull(message = "Role is required")
        Role role
) {
}