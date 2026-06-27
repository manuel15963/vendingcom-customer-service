package com.vendingcom.customer_service.domain.model;

import java.time.LocalDateTime;

/**
 * Cliente de la empresa de vending (empresa, institución o persona).
 */
public record Customer(
        Integer customerId,
        String businessName,
        String tradeName,
        Integer customerTypeId,
        String mainEmail,
        String mainPhone,
        String website,
        Integer customerStatusId,
        Integer createdByUserId,
        Integer updatedByUserId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        // Etiquetas legibles resueltas desde el catálogo (solo lectura; null en escrituras).
        String customerTypeName,
        String customerStatusName
) {
}
