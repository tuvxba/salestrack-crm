package com.salestrack.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salestrack.dto.deal.DealRequest;
import com.salestrack.dto.deal.DealResponse;
import com.salestrack.dto.deal.DealStageHistoryResponse;
import com.salestrack.dto.deal.DealStageUpdateRequest;
import com.salestrack.entity.Company;
import com.salestrack.entity.Deal;
import com.salestrack.entity.DealStageHistory;
import com.salestrack.entity.User;
import com.salestrack.enums.DealStage;
import com.salestrack.enums.Role;
import com.salestrack.exception.InvalidStageTransitionException;
import com.salestrack.exception.ResourceNotFoundException;
import com.salestrack.mapper.DealMapper;
import com.salestrack.repository.DealRepository;
import com.salestrack.repository.DealStageHistoryRepository;
import com.salestrack.repository.UserRepository;

@Service
@Transactional
public class DealService {

    private final DealRepository dealRepository;
    private final CompanyService companyService;
    private final UserRepository userRepository;
    private final DealMapper dealMapper;
    private final DealStageHistoryRepository dealStageHistoryRepository;


    public DealService(
            DealRepository dealRepository,
            CompanyService companyService,
            UserRepository userRepository,
            DealMapper dealMapper,
            DealStageHistoryRepository dealStageHistoryRepository
    ) {
        this.dealRepository = dealRepository;
        this.companyService = companyService;
        this.userRepository = userRepository;
        this.dealMapper = dealMapper;
        this.dealStageHistoryRepository = dealStageHistoryRepository;
    }

    @Transactional(readOnly = true)
    public List<DealResponse> findAll(Long companyId, DealStage stage) {
        User currentUser = getCurrentUser();
        List<Deal> deals;

        if (currentUser.getRole() == Role.SALES_REP) {
            deals = dealRepository.findByAssignedUserId(currentUser.getId());
        } else if (companyId != null) {
            deals = dealRepository.findByCompanyId(companyId);
        } else if (stage != null) {
            deals = dealRepository.findByStage(stage);
        } else {
            deals = dealRepository.findAll();
        }

        return deals.stream()
                .map(dealMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DealResponse findById(Long id) {
        Deal deal = getDeal(id);
        checkDealAccess(deal, getCurrentUser());
        return dealMapper.toResponse(deal);
    }

    public DealResponse create(DealRequest request) {
        Company company = companyService.getCompany(request.companyId());
        User currentUser = getCurrentUser();
        User assignedUser = resolveAssignedUser(request.assignedUserId(), currentUser);

        Deal deal = dealMapper.toEntity(request);
        deal.setStage(DealStage.NEW);
        deal.setCompany(company);
        deal.setAssignedUser(assignedUser);

        Deal saved = dealRepository.save(deal);
        logStageChange(saved, null, DealStage.NEW, currentUser);

        return dealMapper.toResponse(saved);
    }

    private User resolveAssignedUser(Long requestedUserId, User currentUser) {
        if (requestedUserId == null) {
            return currentUser;
        }

        boolean canAssignToOthers = currentUser.getRole() == Role.MANAGER
                || currentUser.getRole() == Role.ADMIN;

        if (!canAssignToOthers) {
            throw new AccessDeniedException("Only managers or admins can assign deals to other users");
        }

        return userRepository.findById(requestedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + requestedUserId));
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
    }

    private void checkDealAccess(Deal deal, User currentUser) {
        boolean isOwner = deal.getAssignedUser().getId().equals(currentUser.getId());
        boolean isPrivileged = currentUser.getRole() == Role.MANAGER || currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isPrivileged) {
            throw new AccessDeniedException("You do not have access to this deal");
        }
    }

    public DealResponse updateStage(Long id, DealStageUpdateRequest request) {
        Deal deal = getDeal(id);
        User currentUser = getCurrentUser();
        checkDealAccess(deal, currentUser);

        DealStage currentStage = deal.getStage();
        DealStage targetStage = request.stage();

        if (!currentStage.canTransitionTo(targetStage)) {
            throw new InvalidStageTransitionException(
                    "Cannot transition deal from " + currentStage + " to " + targetStage
            );
        }

        deal.setStage(targetStage);
        Deal saved = dealRepository.save(deal);
        logStageChange(saved, currentStage, targetStage, currentUser);

        return dealMapper.toResponse(saved);
    }

    public void delete(Long id) {
        Deal deal = getDeal(id);
        checkDealAccess(deal, getCurrentUser());
        dealRepository.delete(deal);
    }

    private Deal getDeal(Long id) {
        return dealRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deal not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<DealStageHistoryResponse> getStageHistory(Long id) {
        Deal deal = getDeal(id);
        checkDealAccess(deal, getCurrentUser());
        return dealStageHistoryRepository.findByDealIdOrderByCreatedAtAsc(id).stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private void logStageChange(Deal deal, DealStage fromStage, DealStage toStage, User changedBy) {
        DealStageHistory history = new DealStageHistory();
        history.setDeal(deal);
        history.setFromStage(fromStage);
        history.setToStage(toStage);
        history.setChangedBy(changedBy);
        dealStageHistoryRepository.save(history);
    }

    private DealStageHistoryResponse toHistoryResponse(DealStageHistory history) {
        return new DealStageHistoryResponse(
                history.getId(),
                history.getFromStage(),
                history.getToStage(),
                history.getChangedBy().getName(),
                history.getCreatedAt()
        );
    }
    
    public Deal getAccessibleDeal(Long id) {
        Deal deal = getDeal(id);
        checkDealAccess(deal, getCurrentUser());
        return deal;
    }
    
}