package com.salestrack.controller;

import com.salestrack.dto.contact.ContactRequest;
import com.salestrack.dto.contact.ContactResponse;
import com.salestrack.service.ContactService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public List<ContactResponse> findAll(@RequestParam(required = false) Long companyId) {
        return contactService.findAll(companyId);
    }

    @GetMapping("/{id}")
    public ContactResponse findById(@PathVariable Long id) {
        return contactService.findById(id);
    }

    @PostMapping
    public ResponseEntity<ContactResponse> create(@Valid @RequestBody ContactRequest request) {
        ContactResponse response = contactService.create(request);
        return ResponseEntity
                .created(URI.create("/api/contacts/" + response.id()))
                .body(response);
    }

    @PutMapping("/{id}")
    public ContactResponse update(@PathVariable Long id, @Valid @RequestBody ContactRequest request) {
        return contactService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contactService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
