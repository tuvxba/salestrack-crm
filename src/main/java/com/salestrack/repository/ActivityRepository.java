package com.salestrack.repository;

import com.salestrack.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByDealIdOrderByOccurredAtDesc(Long dealId);

    List<Activity> findByContactIdOrderByOccurredAtDesc(Long contactId);

    @Query("SELECT a FROM Activity a " +
           "LEFT JOIN FETCH a.deal d LEFT JOIN FETCH d.assignedUser " +
           "LEFT JOIN FETCH a.contact " +
           "LEFT JOIN FETCH a.loggedBy " +
           "ORDER BY a.occurredAt DESC")
    List<Activity> findAllWithRelationsOrderByOccurredAtDesc();
}