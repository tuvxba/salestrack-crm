package com.salestrack.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.salestrack.dto.company.CompanyRequest;
import com.salestrack.dto.company.CompanyResponse;
import com.salestrack.entity.Company;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "contacts", ignore = true)
    Company toEntity(CompanyRequest request);

    CompanyResponse toResponse(Company company);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "contacts", ignore = true)
    void updateEntity(CompanyRequest request, @MappingTarget Company company);
}