package com.salestrack.dto.deal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.salestrack.enums.DealStage;

public record DealResponse(
        Long id,
        String title,
        BigDecimal amount,
        DealStage stage,
        LocalDate expectedCloseDate,
        Long companyId,
        String companyName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}