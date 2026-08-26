package com.salestrack.dto.lead;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LeadConvertRequest(
        @NotNull(message = "Company id is required")
        Long companyId,

        @NotNull(message = "Deal title is required")
        @Size(max = 200, message = "Title must be at most 200 characters")
        String dealTitle,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Amount must be zero or positive")
        BigDecimal amount,

        LocalDate expectedCloseDate
) {
}