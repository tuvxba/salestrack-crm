package com.salestrack.dto.report;

import java.math.BigDecimal;

public record UserPerformanceResponse(
        Long userId,
        String userName,
        Long totalDeals,
        Long wonDeals,
        BigDecimal totalWonAmount
) {
}