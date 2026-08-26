package com.salestrack.dto.lead;

import com.salestrack.enums.LeadStatus;
import jakarta.validation.constraints.NotNull;

public record LeadStatusUpdateRequest(
        @NotNull(message = "Status is required")
        LeadStatus status
) {
}