package com.salestrack.dto.contact;

import java.time.LocalDateTime;

public record ContactResponse(
        Long id,
        String name,
        String email,
        String phone,
        String position,
        Long companyId,
        String companyName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}