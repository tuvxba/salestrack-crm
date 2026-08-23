package com.salestrack.service;

import com.salestrack.dto.deal.DealRequest;
import com.salestrack.dto.deal.DealResponse;
import com.salestrack.dto.deal.DealStageUpdateRequest;
import com.salestrack.entity.Company;
import com.salestrack.entity.Deal;
import com.salestrack.enums.DealStage;
import com.salestrack.exception.InvalidStageTransitionException;
import com.salestrack.exception.ResourceNotFoundException;
import com.salestrack.mapper.DealMapper;
import com.salestrack.repository.DealRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DealService {

    private final DealRepository dealRepository;
    private final CompanyService companyService;
    private final DealMapper dealMapper;

    public DealService(
            DealRepository dealRepository,
            CompanyService companyService,
            DealMapper dealMapper
    ) {
        this.dealRepository = dealRepository;
        this.companyService = companyService;
        this.dealMapper = dealMapper;
    }

    @Transactional(readOnly = true)
    public List<DealResponse> findAll(Long companyId, DealStage stage) {
        List<Deal> deals;

        if (companyId != null) {
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
        return dealMapper.toResponse(getDeal(id));
    }

    public DealResponse create(DealRequest request) {
        Company company = companyService.getCompany(request.companyId());

        Deal deal = dealMapper.toEntity(request);
        deal.setStage(DealStage.NEW);
        deal.setCompany(company);

        return dealMapper.toResponse(dealRepository.save(deal));
    }

    public DealResponse updateStage(Long id, DealStageUpdateRequest request) {
        Deal deal = getDeal(id);
        DealStage currentStage = deal.getStage();
        DealStage targetStage = request.stage();

        if (!currentStage.canTransitionTo(targetStage)) {
            throw new InvalidStageTransitionException(
                    "Cannot transition deal from " + currentStage + " to " + targetStage
            );
        }

        deal.setStage(targetStage);
        return dealMapper.toResponse(dealRepository.save(deal));
    }

    public void delete(Long id) {
        Deal deal = getDeal(id);
        dealRepository.delete(deal);
    }

    private Deal getDeal(Long id) {
        return dealRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deal not found with id: " + id));
    }
}