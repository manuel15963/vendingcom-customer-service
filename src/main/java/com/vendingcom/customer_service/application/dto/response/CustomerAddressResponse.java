package com.vendingcom.customer_service.application.dto.response;

import com.vendingcom.customer_service.domain.model.CustomerAddress;

import java.time.LocalDateTime;

public record CustomerAddressResponse(
        Integer addressId,
        Integer customerId,
        Integer addressTypeId,
        String addressTypeName,
        String addressLine,
        String district,
        String province,
        String department,
        String country,
        String reference,
        Boolean isPrimary,
        Integer addressStatusId,
        String addressStatusName,
        Integer createdByUserId,
        Integer updatedByUserId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CustomerAddressResponse fromDomain(CustomerAddress address) {
        return new CustomerAddressResponse(
                address.addressId(),
                address.customerId(),
                address.addressTypeId(),
                address.addressTypeName(),
                address.addressLine(),
                address.district(),
                address.province(),
                address.department(),
                address.country(),
                address.reference(),
                address.isPrimary(),
                address.addressStatusId(),
                address.addressStatusName(),
                address.createdByUserId(),
                address.updatedByUserId(),
                address.createdAt(),
                address.updatedAt()
        );
    }
}
