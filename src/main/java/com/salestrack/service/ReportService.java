package com.salestrack.service;

import com.salestrack.dto.report.*;
import com.salestrack.entity.User;
import com.salestrack.enums.DealStage;
import com.salestrack.enums.LeadStatus;
import com.salestrack.enums.Role;
import com.salestrack.repository.DealRepository;
import com.salestrack.repository.LeadRepository;
import com.salestrack.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final DealRepository dealRepository;
    private final LeadRepository leadRepository;
    private final UserRepository userRepository;

    public ReportService(DealRepository dealRepository, LeadRepository leadRepository, UserRepository userRepository) {
        this.dealRepository = dealRepository;
        this.leadRepository = leadRepository;
        this.userRepository = userRepository;
    }

    public PipelineSummaryResponse getPipelineSummary() {
        Long userId = scopeToCurrentUserIfSalesRep();
        List<StageSummary> byStage = dealRepository.aggregateByStage(userId);

        long totalOpenDeals = byStage.stream()
                .filter(s -> s.stage() != DealStage.WON && s.stage() != DealStage.LOST)
                .mapToLong(StageSummary::count)
                .sum();

        BigDecimal totalOpenAmount = byStage.stream()
                .filter(s -> s.stage() != DealStage.WON && s.stage() != DealStage.LOST)
                .map(StageSummary::totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PipelineSummaryResponse(totalOpenDeals, totalOpenAmount, byStage);
    }

    public WonSummaryResponse getWonSummary(Integer year, Integer month) {
        Long userId = scopeToCurrentUserIfSalesRep();

        LocalDate now = LocalDate.now();
        int resolvedYear = year != null ? year : now.getYear();
        int resolvedMonth = month != null ? month : now.getMonthValue();

        LocalDateTime start = LocalDate.of(resolvedYear, resolvedMonth, 1).atStartOfDay();
        LocalDateTime end = start.plusMonths(1);

        long count = dealRepository.countWonBetween(start, end, userId);
        BigDecimal amount = dealRepository.sumWonAmountBetween(start, end, userId);

        return new WonSummaryResponse(resolvedYear, resolvedMonth, count, amount);
    }

    public ConversionRateResponse getConversionRate() {
        long total = leadRepository.count();
        long converted = leadRepository.countByStatus(LeadStatus.CONVERTED);
        double rate = total == 0 ? 0.0 : (converted * 100.0) / total;

        return new ConversionRateResponse(total, converted, rate);
    }

    public List<UserPerformanceResponse> getUserPerformance() {
        return dealRepository.aggregateUserPerformance();
    }

    private Long scopeToCurrentUserIfSalesRep() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));

        return currentUser.getRole() == Role.SALES_REP ? currentUser.getId() : null;
    }
}