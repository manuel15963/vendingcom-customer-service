package com.vendingcom.customer_service.application.dto.response;

import com.vendingcom.customer_service.domain.model.CustomerParameter;

public record CustomerParameterResponse(
        Integer parameterId,
        String parameterGroup,
        String parameterCode,
        String parameterValue,
        String description,
        Integer sortOrder
) {
    public static CustomerParameterResponse fromDomain(CustomerParameter parameter) {
        return new CustomerParameterResponse(
                parameter.parameterId(),
                parameter.parameterGroup(),
                parameter.parameterCode(),
                parameter.parameterValue(),
                parameter.description(),
                parameter.sortOrder()
        );
    }
}
