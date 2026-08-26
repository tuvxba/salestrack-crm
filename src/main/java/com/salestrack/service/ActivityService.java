package com.salestrack.service;

import com.salestrack.dto.activity.ActivityRequest;
import com.salestrack.dto.activity.ActivityResponse;
import com.salestrack.entity.Activity;
import com.salestrack.entity.Deal;
import com.salestrack.entity.Contact;
import com.salestrack.entity.User;
import com.salestrack.exception.ResourceNotFoundException;
import com.salestrack.mapper.ActivityMapper;
import com.salestrack.repository.ActivityRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.salestrack.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final DealService dealService;
    private final ContactService contactService;
    private final UserRepository userRepository;
    private final ActivityMapper activityMapper;

    public ActivityService(
            ActivityRepository activityRepository,
            DealService dealService,
            ContactService contactService,
            UserRepository userRepository,
            ActivityMapper activityMapper
    ) {
        this.activityRepository = activityRepository;
        this.dealService = dealService;
        this.contactService = contactService;
        this.userRepository = userRepository;
        this.activityMapper = activityMapper;
    }

    public ActivityResponse create(ActivityRequest request) {
        boolean hasDeal = request.dealId() != null;
        boolean hasContact = request.contactId() != null;

        if (hasDeal == hasContact) {
            throw new IllegalArgumentException("Exactly one of dealId or contactId must be provided");
        }

        Activity activity = activityMapper.toEntity(request);
        activity.setOccurredAt(request.occurredAt() != null ? request.occurredAt() : LocalDateTime.now());
        activity.setLoggedBy(getCurrentUser());

        if (hasDeal) {
            Deal deal = dealService.getAccessibleDeal(request.dealId());
            activity.setDeal(deal);
        } else {
            Contact contact = contactService.getContact(request.contactId());
            activity.setContact(contact);
        }

        return activityMapper.toResponse(activityRepository.save(activity));
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> findByDeal(Long dealId) {
        dealService.getAccessibleDeal(dealId);
        return activityRepository.findByDealIdOrderByOccurredAtDesc(dealId)
                .stream()
                .map(activityMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> findByContact(Long contactId) {
        contactService.getContact(contactId);
        return activityRepository.findByContactIdOrderByOccurredAtDesc(contactId)
                .stream()
                .map(activityMapper::toResponse)
                .toList();
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
    }
}