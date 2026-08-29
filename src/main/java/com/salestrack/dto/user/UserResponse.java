package com.salestrack.dto.user;

import java.time.LocalDateTime;

import com.salestrack.enums.Role;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        LocalDateTime createdAt
) {
}