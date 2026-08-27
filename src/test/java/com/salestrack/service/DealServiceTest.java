package com.salestrack.service;

import com.salestrack.dto.deal.DealStageUpdateRequest;
import com.salestrack.entity.Company;
import com.salestrack.entity.Deal;
import com.salestrack.entity.User;
import com.salestrack.enums.DealStage;
import com.salestrack.enums.Role;
import com.salestrack.exception.InvalidStageTransitionException;
import com.salestrack.mapper.DealMapper;
import com.salestrack.repository.DealRepository;
import com.salestrack.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DealServiceTest {

    @Mock
    private DealRepository dealRepository;

    @Mock
    private CompanyService companyService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DealMapper dealMapper;

    @InjectMocks
    private DealService dealService;

    private User ownerUser;
    private User otherUser;
    private Deal deal;

    @BeforeEach
    void setUp() {
        ownerUser = new User();
        ownerUser.setId(1L);
        ownerUser.setEmail("owner@salestrack.com");
        ownerUser.setRole(Role.SALES_REP);

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail("other@salestrack.com");
        otherUser.setRole(Role.SALES_REP);

        Company company = new Company();
        company.setId(1L);

        deal = new Deal();
        deal.setId(100L);
        deal.setStage(DealStage.NEW);
        deal.setCompany(company);
        deal.setAssignedUser(ownerUser);
    }

    private void authenticateAs(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getEmail(), null)
        );
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @Test
    void updateStage_throwsException_whenTransitionIsInvalid() {
        authenticateAs(ownerUser);
        when(dealRepository.findById(100L)).thenReturn(Optional.of(deal));

        DealStageUpdateRequest request = new DealStageUpdateRequest(DealStage.WON);

        assertThrows(InvalidStageTransitionException.class, () -> dealService.updateStage(100L, request));
    }

    @Test
    void findById_throwsAccessDenied_whenUserIsNotOwnerOrPrivileged() {
        authenticateAs(otherUser);
        when(dealRepository.findById(100L)).thenReturn(Optional.of(deal));

        assertThrows(AccessDeniedException.class, () -> dealService.findById(100L));
    }

    @Test
    void findById_succeeds_whenUserIsOwner() {
        authenticateAs(ownerUser);
        when(dealRepository.findById(100L)).thenReturn(Optional.of(deal));
        when(dealMapper.toResponse(any(Deal.class))).thenReturn(null);

        assertEquals(null, dealService.findById(100L));
    }
}