package com.salestrack.repository;

import com.salestrack.dto.report.StageSummary;
import com.salestrack.dto.report.UserPerformanceResponse;
import com.salestrack.entity.Deal;
import com.salestrack.enums.DealStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface DealRepository extends JpaRepository<Deal, Long> {

    List<Deal> findByCompanyId(Long companyId);

    List<Deal> findByStage(DealStage stage);

    List<Deal> findByAssignedUserId(Long assignedUserId);

    @Query("SELECT new com.salestrack.dto.report.StageSummary(d.stage, COUNT(d), COALESCE(SUM(d.amount), 0)) " +
           "FROM Deal d WHERE (:userId IS NULL OR d.assignedUser.id = :userId) GROUP BY d.stage")
    List<StageSummary> aggregateByStage(@Param("userId") Long userId);

    @Query("SELECT COUNT(d) FROM Deal d WHERE d.stage = com.salestrack.enums.DealStage.WON " +
           "AND d.updatedAt >= :start AND d.updatedAt < :end " +
           "AND (:userId IS NULL OR d.assignedUser.id = :userId)")
    long countWonBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Deal d WHERE d.stage = com.salestrack.enums.DealStage.WON " +
           "AND d.updatedAt >= :start AND d.updatedAt < :end " +
           "AND (:userId IS NULL OR d.assignedUser.id = :userId)")
    BigDecimal sumWonAmountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("userId") Long userId);

    @Query("SELECT new com.salestrack.dto.report.UserPerformanceResponse(u.id, u.name, COUNT(d), " +
           "SUM(CASE WHEN d.stage = com.salestrack.enums.DealStage.WON THEN 1L ELSE 0L END), " +
           "COALESCE(SUM(CASE WHEN d.stage = com.salestrack.enums.DealStage.WON THEN d.amount ELSE 0 END), 0)) " +
           "FROM Deal d JOIN d.assignedUser u GROUP BY u.id, u.name")
    List<UserPerformanceResponse> aggregateUserPerformance();
}