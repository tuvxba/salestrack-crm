package com.salestrack.controller;

import com.salestrack.dto.deal.DealResponse;
import com.salestrack.dto.lead.*;
import com.salestrack.service.LeadService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leads")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @GetMapping
    public List<LeadResponse> findAll() {
        return leadService.findAll();
    }

    @GetMapping("/{id}")
    public LeadResponse findById(@PathVariable Long id) {
        return leadService.findById(id);
    }

    @PostMapping
    public ResponseEntity<LeadResponse> create(@Valid @RequestBody LeadRequest request) {
        LeadResponse response = leadService.create(request);
        return ResponseEntity
                .created(URI.create("/api/leads/" + response.id()))
                .body(response);
    }

    @PatchMapping("/{id}/status")
    public LeadResponse updateStatus(@PathVariable Long id, @Valid @RequestBody LeadStatusUpdateRequest request) {
        return leadService.updateStatus(id, request);
    }

    @PostMapping("/{id}/convert")
    public ResponseEntity<DealResponse> convert(@PathVariable Long id, @Valid @RequestBody LeadConvertRequest request) {
        DealResponse response = leadService.convert(id, request);
        return ResponseEntity
                .created(URI.create("/api/deals/" + response.id()))
                .body(response);
    }
}