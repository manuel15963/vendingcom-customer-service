package com.vendingcom.customer_service.application.dto.response;

import com.vendingcom.customer_service.domain.model.CustomerContact;

import java.time.LocalDateTime;

public record CustomerContactResponse(
        Integer contactId,
        Integer customerId,
        String fullName,
        String position,
        String email,
        String phone,
        Boolean isPrimary,
        Integer contactStatusId,
        String contactStatusName,
        Integer createdByUserId,
        Integer updatedByUserId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CustomerContactResponse fromDomain(CustomerContact contact) {
        return new CustomerContactResponse(
                contact.contactId(),
                contact.customerId(),
                contact.fullName(),
                contact.position(),
                contact.email(),
                contact.phone(),
                contact.isPrimary(),
                contact.contactStatusId(),
                contact.contactStatusName(),
                contact.createdByUserId(),
                contact.updatedByUserId(),
                contact.createdAt(),
                contact.updatedAt()
        );
    }
}
