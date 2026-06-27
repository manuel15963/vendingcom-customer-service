package com.vendingcom.customer_service.application.dto.response;

import com.vendingcom.customer_service.domain.model.CustomerDocument;

import java.time.LocalDateTime;

public record CustomerDocumentResponse(
        Integer documentId,
        Integer customerId,
        Integer documentTypeId,
        String documentTypeName,
        String documentNumber,
        String fileUrl,
        Boolean isPrimary,
        Integer documentStatusId,
        String documentStatusName,
        Integer createdByUserId,
        Integer updatedByUserId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CustomerDocumentResponse fromDomain(CustomerDocument document) {
        return new CustomerDocumentResponse(
                document.documentId(),
                document.customerId(),
                document.documentTypeId(),
                document.documentTypeName(),
                document.documentNumber(),
                document.fileUrl(),
                document.isPrimary(),
                document.documentStatusId(),
                document.documentStatusName(),
                document.createdByUserId(),
                document.updatedByUserId(),
                document.createdAt(),
                document.updatedAt()
        );
    }
}
