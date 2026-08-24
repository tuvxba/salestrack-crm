package com.salestrack.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.salestrack.dto.deal.DealRequest;
import com.salestrack.dto.deal.DealResponse;
import com.salestrack.entity.Deal;

@Mapper(componentModel = "spring")
public interface DealMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "stage", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "assignedUser", ignore = true)
    Deal toEntity(DealRequest request);

    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "companyName", source = "company.name")
    @Mapping(target = "assignedUserId", source = "assignedUser.id")
    @Mapping(target = "assignedUserName", source = "assignedUser.name")
    DealResponse toResponse(Deal deal);
}