package com.salestrack.dto.lead;

import com.salestrack.enums.LeadSource;
import com.salestrack.enums.LeadStatus;

import java.time.LocalDateTime;

public record LeadResponse(
        Long id,
        String name,
        String email,
        String phone,
        String companyName,
        LeadSource source,
        LeadStatus status,
        Long assignedUserId,
        String assignedUserName,
        Long convertedDealId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}