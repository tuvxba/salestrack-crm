package com.salestrack.repository;

import com.salestrack.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    boolean existsByNameIgnoreCase(String name);
}
