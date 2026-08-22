package com.salestrack.dto.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyRequest(
        @NotBlank(message = "Company name is required")
        @Size(max = 160, message = "Company name must be at most 160 characters")
        String name,

        @Size(max = 100, message = "Sector must be at most 100 characters")
        String sector,

        @Size(max = 255, message = "Website must be at most 255 characters")
        String website
) {
}
