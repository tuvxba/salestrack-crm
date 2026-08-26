package com.salestrack.repository;

import com.salestrack.entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeadRepository extends JpaRepository<Lead, Long> {

    List<Lead> findByAssignedUserId(Long assignedUserId);
}