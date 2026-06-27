package com.vendingcom.customer_service.domain.model;

import java.time.LocalDateTime;

public record CustomerDocument(
        Integer documentId,
        Integer customerId,
        Integer documentTypeId,
        String documentNumber,
        String fileUrl,
        Boolean isPrimary,
        Integer documentStatusId,
        Integer createdByUserId,
        Integer updatedByUserId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String documentTypeName,
        String documentStatusName
) {
}
