package com.salestrack.dto.report;

import java.math.BigDecimal;

public record WonSummaryResponse(
        int year,
        int month,
        long wonDealCount,
        BigDecimal totalWonAmount
) {
}