package com.salestrack.dto.activity;

import com.salestrack.enums.ActivityType;

import java.time.LocalDateTime;

public record ActivityResponse(
        Long id,
        ActivityType type,
        String description,
        LocalDateTime occurredAt,
        Long dealId,
        String dealTitle,
        Long contactId,
        String contactName,
        Long loggedByUserId,
        String loggedByUserName,
        LocalDateTime createdAt
) {
}