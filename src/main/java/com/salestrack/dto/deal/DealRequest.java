package com.salestrack.dto.deal;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DealRequest(
        @NotBlank(message = "Deal title is required")
        @Size(max = 200, message = "Title must be at most 200 characters")
        String title,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Amount must be zero or positive")
        BigDecimal amount,

        LocalDate expectedCloseDate,

        @NotNull(message = "Company id is required")
        Long companyId,

        Long assignedUserId
) {
}