package com.salestrack.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.salestrack.dto.activity.ActivityRequest;
import com.salestrack.dto.activity.ActivityResponse;
import com.salestrack.enums.ActivityType;
import com.salestrack.service.ActivityService;

import jakarta.validation.Valid;

@RestController
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping("/api/activities")
    public List<ActivityResponse> findAll(@RequestParam(required = false) ActivityType type) {
        return activityService.findAll(type);
    }

    @PostMapping("/api/activities")
    public ResponseEntity<ActivityResponse> create(@Valid @RequestBody ActivityRequest request) {
        ActivityResponse response = activityService.create(request);
        return ResponseEntity
                .created(URI.create("/api/activities/" + response.id()))
                .body(response);
    }

    @GetMapping("/api/deals/{dealId}/activities")
    public List<ActivityResponse> findByDeal(@PathVariable Long dealId) {
        return activityService.findByDeal(dealId);
    }

    @GetMapping("/api/contacts/{contactId}/activities")
    public List<ActivityResponse> findByContact(@PathVariable Long contactId) {
        return activityService.findByContact(contactId);
    }
}
