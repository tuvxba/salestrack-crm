package com.salestrack.repository;

import com.salestrack.entity.Contact;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    boolean existsByEmailIgnoreCase(String email);

    List<Contact> findByCompanyId(Long companyId);
}
