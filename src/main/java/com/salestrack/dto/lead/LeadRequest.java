package com.salestrack.dto.lead;

import com.salestrack.enums.LeadSource;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LeadRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 160, message = "Name must be at most 160 characters")
        String name,

        @Email(message = "Email must be valid")
        String email,

        @Size(max = 40, message = "Phone must be at most 40 characters")
        String phone,

        @Size(max = 160, message = "Company name must be at most 160 characters")
        String companyName,

        @NotNull(message = "Source is required")
        LeadSource source
) {
}