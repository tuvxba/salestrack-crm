package com.salestrack.dto.activity;

import com.salestrack.enums.ActivityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ActivityRequest(
        @NotNull(message = "Activity type is required")
        ActivityType type,

        @NotBlank(message = "Description is required")
        @Size(max = 1000, message = "Description must be at most 1000 characters")
        String description,

        LocalDateTime occurredAt,

        Long dealId,

        Long contactId
) {
}