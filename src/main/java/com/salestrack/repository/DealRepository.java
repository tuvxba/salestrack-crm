package com.salestrack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salestrack.entity.Deal;
import com.salestrack.enums.DealStage;

public interface DealRepository extends JpaRepository<Deal, Long> {

    List<Deal> findByCompanyId(Long companyId);

    List<Deal> findByStage(DealStage stage);
}