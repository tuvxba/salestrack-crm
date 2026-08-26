package com.salestrack.controller;

import com.salestrack.dto.report.*;
import com.salestrack.service.ReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/pipeline-summary")
    public PipelineSummaryResponse pipelineSummary() {
        return reportService.getPipelineSummary();
    }

    @GetMapping("/won-summary")
    public WonSummaryResponse wonSummary(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        return reportService.getWonSummary(year, month);
    }

    @GetMapping("/conversion-rate")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ConversionRateResponse conversionRate() {
        return reportService.getConversionRate();
    }

    @GetMapping("/user-performance")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public List<UserPerformanceResponse> userPerformance() {
        return reportService.getUserPerformance();
    }
}