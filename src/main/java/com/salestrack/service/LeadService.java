package com.salestrack.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salestrack.dto.deal.DealRequest;
import com.salestrack.dto.deal.DealResponse;
import com.salestrack.dto.lead.LeadConvertRequest;
import com.salestrack.dto.lead.LeadRequest;
import com.salestrack.dto.lead.LeadResponse;
import com.salestrack.dto.lead.LeadStatusUpdateRequest;
import com.salestrack.entity.Deal;
import com.salestrack.entity.Lead;
import com.salestrack.entity.User;
import com.salestrack.enums.LeadStatus;
import com.salestrack.enums.Role;
import com.salestrack.exception.ResourceNotFoundException;
import com.salestrack.mapper.LeadMapper;
import com.salestrack.repository.LeadRepository;
import com.salestrack.repository.UserRepository;

@Service
@Transactional
public class LeadService {

    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final DealService dealService;
    private final LeadMapper leadMapper;

    public LeadService(
            LeadRepository leadRepository,
            UserRepository userRepository,
            DealService dealService,
            LeadMapper leadMapper
    ) {
        this.leadRepository = leadRepository;
        this.userRepository = userRepository;
        this.dealService = dealService;
        this.leadMapper = leadMapper;
    }

    @Transactional(readOnly = true)
    public List<LeadResponse> findAll() {
        User currentUser = getCurrentUser();
        List<Lead> leads = currentUser.getRole() == Role.SALES_REP
                ? leadRepository.findByAssignedUserId(currentUser.getId())
                : leadRepository.findAll();

        return leads.stream().map(leadMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public LeadResponse findById(Long id) {
        Lead lead = getLead(id);
        checkLeadAccess(lead, getCurrentUser());
        return leadMapper.toResponse(lead);
    }

    public LeadResponse create(LeadRequest request) {
        Lead lead = leadMapper.toEntity(request);
        lead.setStatus(LeadStatus.NEW);
        lead.setAssignedUser(getCurrentUser());

        return leadMapper.toResponse(leadRepository.save(lead));
    }

    public LeadResponse updateStatus(Long id, LeadStatusUpdateRequest request) {
        Lead lead = getLead(id);
        checkLeadAccess(lead, getCurrentUser());

        if (lead.getStatus() == LeadStatus.CONVERTED) {
            throw new IllegalArgumentException("Cannot change status of an already converted lead");
        }

        lead.setStatus(request.status());
        return leadMapper.toResponse(leadRepository.save(lead));
    }

    public DealResponse convert(Long id, LeadConvertRequest request) {
        Lead lead = getLead(id);
        checkLeadAccess(lead, getCurrentUser());

        if (lead.getStatus() == LeadStatus.CONVERTED) {
            throw new IllegalArgumentException("Lead has already been converted");
        }

        DealRequest dealRequest = new DealRequest(
                request.dealTitle(),
                request.amount(),
                request.expectedCloseDate(),
                request.companyId(),
                null
        );

        DealResponse dealResponse = dealService.create(dealRequest);

        Deal deal = dealService.getAccessibleDeal(dealResponse.id());
        lead.setStatus(LeadStatus.CONVERTED);
        lead.setConvertedDeal(deal);
        leadRepository.save(lead);

        return dealResponse;
    }

    private void checkLeadAccess(Lead lead, User currentUser) {
        boolean isOwner = lead.getAssignedUser().getId().equals(currentUser.getId());
        boolean isPrivileged = currentUser.getRole() == Role.MANAGER || currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isPrivileged) {
            throw new AccessDeniedException("You do not have access to this lead");
        }
    }

    private Lead getLead(Long id) {
        return leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id));
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
    }

    public void delete(Long id) {
        Lead lead = getLead(id);
        checkLeadAccess(lead, getCurrentUser());

        if (lead.getStatus() == LeadStatus.CONVERTED) {
            throw new IllegalArgumentException("Cannot delete a lead that has already been converted to a deal");
        }

        leadRepository.delete(lead);
    }
}