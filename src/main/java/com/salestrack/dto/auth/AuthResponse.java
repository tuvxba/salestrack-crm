package com.salestrack.dto.auth;

public record AuthResponse(
        String token,
        String email,
        String role
) {
}