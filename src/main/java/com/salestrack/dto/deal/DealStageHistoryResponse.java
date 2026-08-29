package com.salestrack.dto.deal;

import java.time.LocalDateTime;

import com.salestrack.enums.DealStage;

public record DealStageHistoryResponse(
        Long id,
        DealStage fromStage,
        DealStage toStage,
        String changedByName,
        LocalDateTime changedAt
) {
}