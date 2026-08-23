package com.salestrack.dto.deal;

import com.salestrack.enums.DealStage;

import jakarta.validation.constraints.NotNull;

public record DealStageUpdateRequest(
        @NotNull(message = "Stage is required")
        DealStage stage
) {
}