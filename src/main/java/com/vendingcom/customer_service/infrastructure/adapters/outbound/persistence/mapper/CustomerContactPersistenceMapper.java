package com.vendingcom.customer_service.infrastructure.adapters.outbound.persistence.mapper;

import com.vendingcom.customer_service.domain.model.CustomerContact;
import com.vendingcom.customer_service.infrastructure.adapters.outbound.persistence.entity.CustomerContactEntity;
import org.springframework.stereotype.Component;

@Component
public class CustomerContactPersistenceMapper {

    public CustomerContact toDomain(CustomerContactEntity entity) {
        if (entity == null) {
            return null;
        }
        return new CustomerContact(
                entity.getContactId(),
                entity.getCustomerId(),
                entity.getFullName(),
                entity.getPosition(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getIsPrimary(),
                entity.getContactStatusId(),
                entity.getCreatedByUserId(),
                entity.getUpdatedByUserId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                null    // contactStatusName se resuelve en lecturas (JOIN)
        );
    }

    public CustomerContactEntity toEntity(CustomerContact domain) {
        if (domain == null) {
            return null;
        }
        return CustomerContactEntity.builder()
                .contactId(domain.contactId())
                .customerId(domain.customerId())
                .fullName(domain.fullName())
                .position(domain.position())
                .email(domain.email())
                .phone(domain.phone())
                .isPrimary(domain.isPrimary())
                .contactStatusId(domain.contactStatusId())
                .createdByUserId(domain.createdByUserId())
                .updatedByUserId(domain.updatedByUserId())
                .createdAt(domain.createdAt())
                .updatedAt(domain.updatedAt())
                .build();
    }
}
