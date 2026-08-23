package com.salestrack.mapper;

import com.salestrack.dto.deal.DealRequest;
import com.salestrack.dto.deal.DealResponse;
import com.salestrack.entity.Deal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DealMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "stage", ignore = true)
    @Mapping(target = "company", ignore = true)
    Deal toEntity(DealRequest request);

    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "companyName", source = "company.name")
    DealResponse toResponse(Deal deal);
}