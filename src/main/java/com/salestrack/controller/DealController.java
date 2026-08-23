package com.salestrack.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.salestrack.dto.deal.DealRequest;
import com.salestrack.dto.deal.DealResponse;
import com.salestrack.dto.deal.DealStageUpdateRequest;
import com.salestrack.enums.DealStage;
import com.salestrack.service.DealService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/deals")
public class DealController {

    private final DealService dealService;

    public DealController(DealService dealService) {
        this.dealService = dealService;
    }

    @GetMapping
    public List<DealResponse> findAll(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) DealStage stage
    ) {
        return dealService.findAll(companyId, stage);
    }

    @GetMapping("/{id}")
    public DealResponse findById(@PathVariable Long id) {
        return dealService.findById(id);
    }

    @PostMapping
    public ResponseEntity<DealResponse> create(@Valid @RequestBody DealRequest request) {
        DealResponse response = dealService.create(request);
        return ResponseEntity
                .created(URI.create("/api/deals/" + response.id()))
                .body(response);
    }

    @PatchMapping("/{id}/stage")
    public DealResponse updateStage(
            @PathVariable Long id,
            @Valid @RequestBody DealStageUpdateRequest request
    ) {
        return dealService.updateStage(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dealService.delete(id);
        return ResponseEntity.noContent().build();
    }
}