package com.salestrack.dto.report;

import com.salestrack.enums.DealStage;

import java.math.BigDecimal;

public record StageSummary(
        DealStage stage,
        Long count,
        BigDecimal totalAmount
) {
}