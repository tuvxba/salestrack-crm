package com.salestrack.controller;

import com.salestrack.dto.company.CompanyRequest;
import com.salestrack.dto.company.CompanyResponse;
import com.salestrack.service.CompanyService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public List<CompanyResponse> findAll() {
        return companyService.findAll();
    }

    @GetMapping("/{id}")
    public CompanyResponse findById(@PathVariable Long id) {
        return companyService.findById(id);
    }

    @PostMapping
    public ResponseEntity<CompanyResponse> create(@Valid @RequestBody CompanyRequest request) {
        CompanyResponse response = companyService.create(request);
        return ResponseEntity
                .created(URI.create("/api/companies/" + response.id()))
                .body(response);
    }

    @PutMapping("/{id}")
    public CompanyResponse update(@PathVariable Long id, @Valid @RequestBody CompanyRequest request) {
        return companyService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        companyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
