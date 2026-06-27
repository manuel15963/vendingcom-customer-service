package com.vendingcom.customer_service.domain.model;

import java.time.LocalDateTime;

public record CustomerAddress(
        Integer addressId,
        Integer customerId,
        Integer addressTypeId,
        String addressLine,
        String district,
        String province,
        String department,
        String country,
        String reference,
        Boolean isPrimary,
        Integer addressStatusId,
        Integer createdByUserId,
        Integer updatedByUserId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String addressTypeName,
        String addressStatusName
) {
}
