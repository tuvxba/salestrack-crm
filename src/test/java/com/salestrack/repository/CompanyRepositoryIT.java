package com.salestrack.repository;

import com.salestrack.config.JpaAuditingConfig;
import com.salestrack.entity.Company;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@DataJpaTest
@Import(JpaAuditingConfig.class)
class CompanyRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @org.springframework.beans.factory.annotation.Autowired
    private CompanyRepository companyRepository;

    @Test
    void existsByNameIgnoreCase_findsMatchRegardlessOfCase() {
        Company company = new Company();
        company.setName("Acme Corp");
        companyRepository.save(company);

        assertTrue(companyRepository.existsByNameIgnoreCase("acme corp"));
        assertTrue(companyRepository.existsByNameIgnoreCase("ACME CORP"));
        assertFalse(companyRepository.existsByNameIgnoreCase("Different Company"));
    }
}