package com.salestrack.mapper;

import com.salestrack.dto.activity.ActivityRequest;
import com.salestrack.dto.activity.ActivityResponse;
import com.salestrack.entity.Activity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ActivityMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deal", ignore = true)
    @Mapping(target = "contact", ignore = true)
    @Mapping(target = "loggedBy", ignore = true)
    @Mapping(target = "occurredAt", ignore = true)
    Activity toEntity(ActivityRequest request);

    @Mapping(target = "dealId", source = "deal.id")
    @Mapping(target = "contactId", source = "contact.id")
    @Mapping(target = "loggedByUserId", source = "loggedBy.id")
    @Mapping(target = "loggedByUserName", source = "loggedBy.name")
    ActivityResponse toResponse(Activity activity);
}