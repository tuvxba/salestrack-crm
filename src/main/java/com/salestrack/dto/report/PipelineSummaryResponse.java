package com.salestrack.dto.report;

import java.math.BigDecimal;
import java.util.List;

public record PipelineSummaryResponse(
        long totalOpenDeals,
        BigDecimal totalOpenAmount,
        List<StageSummary> byStage
) {
}