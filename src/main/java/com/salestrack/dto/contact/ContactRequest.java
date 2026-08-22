package com.salestrack.dto.contact;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ContactRequest(
        @NotBlank(message = "Contact name is required")
        @Size(max = 160, message = "Contact name must be at most 160 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 180, message = "Email must be at most 180 characters")
        String email,

        @Size(max = 40, message = "Phone must be at most 40 characters")
        String phone,

        @Size(max = 120, message = "Position must be at most 120 characters")
        String position,

        @NotNull(message = "Company id is required")
        Long companyId
) {
}
