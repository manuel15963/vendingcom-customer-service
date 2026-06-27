package com.vendingcom.customer_service.application.port.input;

import com.vendingcom.customer_service.domain.model.CustomerParameter;
import reactor.core.publisher.Flux;

public interface CustomerParameterUseCase {

    /** Lista los parámetros activos de un grupo (ej: CUSTOMER_TYPE) para combos del frontend. */
    Flux<CustomerParameter> findByGroup(String parameterGroup);

    /** Lista todos los parámetros activos del módulo. */
    Flux<CustomerParameter> findAllActive();
}
