package com.salestrack.dto.report;

public record ConversionRateResponse(
        long totalLeads,
        long convertedLeads,
        double conversionRatePercentage
) {
}