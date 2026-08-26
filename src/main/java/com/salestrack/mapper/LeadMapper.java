package com.salestrack.mapper;

import com.salestrack.dto.lead.LeadRequest;
import com.salestrack.dto.lead.LeadResponse;
import com.salestrack.entity.Lead;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LeadMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "assignedUser", ignore = true)
    @Mapping(target = "convertedDeal", ignore = true)
    Lead toEntity(LeadRequest request);

    @Mapping(target = "assignedUserId", source = "assignedUser.id")
    @Mapping(target = "assignedUserName", source = "assignedUser.name")
    @Mapping(target = "convertedDealId", source = "convertedDeal.id")
    LeadResponse toResponse(Lead lead);
}