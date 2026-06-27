package com.vendingcom.customer_service.infrastructure.adapters.outbound.persistence.mapper;

import com.vendingcom.customer_service.domain.model.Customer;
import com.vendingcom.customer_service.infrastructure.adapters.outbound.persistence.entity.CustomerEntity;
import org.springframework.stereotype.Component;

@Component
public class CustomerPersistenceMapper {

    public Customer toDomain(CustomerEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Customer(
                entity.getCustomerId(),
                entity.getBusinessName(),
                entity.getTradeName(),
                entity.getCustomerTypeId(),
                entity.getMainEmail(),
                entity.getMainPhone(),
                entity.getWebsite(),
                entity.getCustomerStatusId(),
                entity.getCreatedByUserId(),
                entity.getUpdatedByUserId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                null,   // customerTypeName se resuelve solo en lecturas (JOIN)
                null    // customerStatusName se resuelve solo en lecturas (JOIN)
        );
    }

    public CustomerEntity toEntity(Customer domain) {
        if (domain == null) {
            return null;
        }
        return CustomerEntity.builder()
                .customerId(domain.customerId())
                .businessName(domain.businessName())
                .tradeName(domain.tradeName())
                .customerTypeId(domain.customerTypeId())
                .mainEmail(domain.mainEmail())
                .mainPhone(domain.mainPhone())
                .website(domain.website())
                .customerStatusId(domain.customerStatusId())
                .createdByUserId(domain.createdByUserId())
                .updatedByUserId(domain.updatedByUserId())
                .createdAt(domain.createdAt())
                .updatedAt(domain.updatedAt())
                .build();
    }
}
