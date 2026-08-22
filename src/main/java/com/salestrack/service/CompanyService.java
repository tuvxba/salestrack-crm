package com.salestrack.service;

import com.salestrack.dto.company.CompanyRequest;
import com.salestrack.dto.company.CompanyResponse;
import com.salestrack.entity.Company;
import com.salestrack.exception.DuplicateResourceException;
import com.salestrack.exception.ResourceNotFoundException;
import com.salestrack.mapper.CompanyMapper;
import com.salestrack.repository.CompanyRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    public CompanyService(CompanyRepository companyRepository, CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> findAll() {
        return companyRepository.findAll()
                .stream()
                .map(companyMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompanyResponse findById(Long id) {
        return companyMapper.toResponse(getCompany(id));
    }

    public CompanyResponse create(CompanyRequest request) {
        if (companyRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("Company already exists with name: " + request.name());
        }

        Company company = companyMapper.toEntity(request);
        return companyMapper.toResponse(companyRepository.save(company));
    }

    public CompanyResponse update(Long id, CompanyRequest request) {
        Company company = getCompany(id);
        companyMapper.updateEntity(request, company);
        return companyMapper.toResponse(companyRepository.save(company));
    }

    public void delete(Long id) {
        Company company = getCompany(id);
        companyRepository.delete(company);
    }

    @Transactional(readOnly = true)
    public Company getCompany(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
    }
}
