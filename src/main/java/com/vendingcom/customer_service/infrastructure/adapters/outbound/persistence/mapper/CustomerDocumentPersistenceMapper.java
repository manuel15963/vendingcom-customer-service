package com.vendingcom.customer_service.infrastructure.adapters.outbound.persistence.mapper;

import com.vendingcom.customer_service.domain.model.CustomerDocument;
import com.vendingcom.customer_service.infrastructure.adapters.outbound.persistence.entity.CustomerDocumentEntity;
import org.springframework.stereotype.Component;

@Component
public class CustomerDocumentPersistenceMapper {

    public CustomerDocument toDomain(CustomerDocumentEntity entity) {
        if (entity == null) {
            return null;
        }
        return new CustomerDocument(
                entity.getDocumentId(),
                entity.getCustomerId(),
                entity.getDocumentTypeId(),
                entity.getDocumentNumber(),
                entity.getFileUrl(),
                entity.getIsPrimary(),
                entity.getDocumentStatusId(),
                entity.getCreatedByUserId(),
                entity.getUpdatedByUserId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                null,   // documentTypeName se resuelve en lecturas (JOIN)
                null    // documentStatusName se resuelve en lecturas (JOIN)
        );
    }

    public CustomerDocumentEntity toEntity(CustomerDocument domain) {
        if (domain == null) {
            return null;
        }
        return CustomerDocumentEntity.builder()
                .documentId(domain.documentId())
                .customerId(domain.customerId())
                .documentTypeId(domain.documentTypeId())
                .documentNumber(domain.documentNumber())
                .fileUrl(domain.fileUrl())
                .isPrimary(domain.isPrimary())
                .documentStatusId(domain.documentStatusId())
                .createdByUserId(domain.createdByUserId())
                .updatedByUserId(domain.updatedByUserId())
                .createdAt(domain.createdAt())
                .updatedAt(domain.updatedAt())
                .build();
    }
}
