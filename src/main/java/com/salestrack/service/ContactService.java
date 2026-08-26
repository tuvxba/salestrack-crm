package com.salestrack.service;

import com.salestrack.dto.contact.ContactRequest;
import com.salestrack.dto.contact.ContactResponse;
import com.salestrack.entity.Company;
import com.salestrack.entity.Contact;
import com.salestrack.exception.DuplicateResourceException;
import com.salestrack.exception.ResourceNotFoundException;
import com.salestrack.mapper.ContactMapper;
import com.salestrack.repository.ContactRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ContactService {

    private final ContactRepository contactRepository;
    private final CompanyService companyService;
    private final ContactMapper contactMapper;

    public ContactService(
            ContactRepository contactRepository,
            CompanyService companyService,
            ContactMapper contactMapper
    ) {
        this.contactRepository = contactRepository;
        this.companyService = companyService;
        this.contactMapper = contactMapper;
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> findAll(Long companyId) {
        List<Contact> contacts = companyId == null
                ? contactRepository.findAll()
                : contactRepository.findByCompanyId(companyId);

        return contacts.stream()
                .map(contactMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContactResponse findById(Long id) {
        return contactMapper.toResponse(getContact(id));
    }

    public ContactResponse create(ContactRequest request) {
        if (contactRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("Contact already exists with email: " + request.email());
        }

        Company company = companyService.getCompany(request.companyId());
        Contact contact = contactMapper.toEntity(request);
        contact.setCompany(company);

        return contactMapper.toResponse(contactRepository.save(contact));
    }

    public ContactResponse update(Long id, ContactRequest request) {
        Contact contact = getContact(id);
        Company company = companyService.getCompany(request.companyId());

        contactMapper.updateEntity(request, contact);
        contact.setCompany(company);

        return contactMapper.toResponse(contactRepository.save(contact));
    }

    public void delete(Long id) {
        Contact contact = getContact(id);
        contactRepository.delete(contact);
    }
    public Contact getContact(Long id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));
    }
}
