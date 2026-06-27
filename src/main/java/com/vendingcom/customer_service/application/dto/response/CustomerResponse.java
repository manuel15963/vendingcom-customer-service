package com.vendingcom.customer_service.application.dto.response;

import com.vendingcom.customer_service.domain.model.Customer;

import java.time.LocalDateTime;

public record CustomerResponse(
        Integer customerId,
        String businessName,
        String tradeName,
        Integer customerTypeId,
        String customerTypeName,
        String mainEmail,
        String mainPhone,
        String website,
        Integer customerStatusId,
        String customerStatusName,
        Integer createdByUserId,
        Integer updatedByUserId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CustomerResponse fromDomain(Customer customer) {
        return new CustomerResponse(
                customer.customerId(),
                customer.businessName(),
                customer.tradeName(),
                customer.customerTypeId(),
                customer.customerTypeName(),
                customer.mainEmail(),
                customer.mainPhone(),
                customer.website(),
                customer.customerStatusId(),
                customer.customerStatusName(),
                customer.createdByUserId(),
                customer.updatedByUserId(),
                customer.createdAt(),
                customer.updatedAt()
        );
    }
}
