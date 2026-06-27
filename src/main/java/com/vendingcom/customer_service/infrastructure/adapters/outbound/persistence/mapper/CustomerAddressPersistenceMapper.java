package com.vendingcom.customer_service.infrastructure.adapters.outbound.persistence.mapper;

import com.vendingcom.customer_service.domain.model.CustomerAddress;
import com.vendingcom.customer_service.infrastructure.adapters.outbound.persistence.entity.CustomerAddressEntity;
import org.springframework.stereotype.Component;

@Component
public class CustomerAddressPersistenceMapper {

    public CustomerAddress toDomain(CustomerAddressEntity entity) {
        if (entity == null) {
            return null;
        }
        return new CustomerAddress(
                entity.getAddressId(),
                entity.getCustomerId(),
                entity.getAddressTypeId(),
                entity.getAddressLine(),
                entity.getDistrict(),
                entity.getProvince(),
                entity.getDepartment(),
                entity.getCountry(),
                entity.getReference(),
                entity.getIsPrimary(),
                entity.getAddressStatusId(),
                entity.getCreatedByUserId(),
                entity.getUpdatedByUserId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                null,   // addressTypeName se resuelve en lecturas (JOIN)
                null    // addressStatusName se resuelve en lecturas (JOIN)
        );
    }

    public CustomerAddressEntity toEntity(CustomerAddress domain) {
        if (domain == null) {
            return null;
        }
        return CustomerAddressEntity.builder()
                .addressId(domain.addressId())
                .customerId(domain.customerId())
                .addressTypeId(domain.addressTypeId())
                .addressLine(domain.addressLine())
                .district(domain.district())
                .province(domain.province())
                .department(domain.department())
                .country(domain.country())
                .reference(domain.reference())
                .isPrimary(domain.isPrimary())
                .addressStatusId(domain.addressStatusId())
                .createdByUserId(domain.createdByUserId())
                .updatedByUserId(domain.updatedByUserId())
                .createdAt(domain.createdAt())
                .updatedAt(domain.updatedAt())
                .build();
    }
}
