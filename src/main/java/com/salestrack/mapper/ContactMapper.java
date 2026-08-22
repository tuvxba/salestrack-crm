package com.salestrack.mapper;

import com.salestrack.dto.contact.ContactRequest;
import com.salestrack.dto.contact.ContactResponse;
import com.salestrack.entity.Contact;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ContactMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "company", ignore = true)
    Contact toEntity(ContactRequest request);

    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "companyName", source = "company.name")
    ContactResponse toResponse(Contact contact);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "company", ignore = true)
    void updateEntity(ContactRequest request, @MappingTarget Contact contact);
}
