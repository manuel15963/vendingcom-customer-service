package com.vendingcom.customer_service.domain.model;

import java.time.LocalDateTime;

public record CustomerContact(
        Integer contactId,
        Integer customerId,
        String fullName,
        String position,
        String email,
        String phone,
        Boolean isPrimary,
        Integer contactStatusId,
        Integer createdByUserId,
        Integer updatedByUserId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String contactStatusName
) {
}
