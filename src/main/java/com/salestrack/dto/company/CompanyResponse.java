package com.salestrack.dto.company;

import java.time.LocalDateTime;

public record CompanyResponse(
        Long id,
        String name,
        String sector,
        String website,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}