package com.vendingcom.customer_service.application.port.input;

import com.vendingcom.customer_service.application.dto.response.CustomerDetailResponse;
import reactor.core.publisher.Mono;

public interface CustomerDetailUseCase {

    /** Devuelve el cliente junto con sus contactos, direcciones y documentos. */
    Mono<CustomerDetailResponse> findDetail(Integer customerId);
}
