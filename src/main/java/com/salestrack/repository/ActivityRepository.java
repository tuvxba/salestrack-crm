package com.salestrack.repository;

import com.salestrack.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByDealIdOrderByOccurredAtDesc(Long dealId);

    List<Activity> findByContactIdOrderByOccurredAtDesc(Long contactId);
}