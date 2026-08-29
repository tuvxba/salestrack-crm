package com.salestrack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salestrack.entity.DealStageHistory;

public interface DealStageHistoryRepository extends JpaRepository<DealStageHistory, Long> {
    List<DealStageHistory> findByDealIdOrderByCreatedAtAsc(Long dealId);
}