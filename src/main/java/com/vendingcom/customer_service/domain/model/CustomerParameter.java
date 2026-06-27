package com.vendingcom.customer_service.domain.model;

public record CustomerParameter(
        Integer parameterId,
        String parameterGroup,
        String parameterCode,
        String parameterValue,
        String description,
        Integer sortOrder,
        Integer parameterStatus
) {
}
