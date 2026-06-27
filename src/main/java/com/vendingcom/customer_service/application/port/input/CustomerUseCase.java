package com.vendingcom.customer_service.application.port.input;

import com.vendingcom.customer_service.application.dto.request.CreateCustomerRequest;
import com.vendingcom.customer_service.application.dto.request.UpdateCustomerRequest;
import com.vendingcom.customer_service.application.dto.response.PagedResponse;
import com.vendingcom.customer_service.domain.model.Customer;
import reactor.core.publisher.Mono;

public interface CustomerUseCase {

    Mono<Customer> create(CreateCustomerRequest request);

    Mono<Customer> update(Integer customerId, UpdateCustomerRequest request);

    Mono<Customer> findById(Integer customerId);

    /** Búsqueda paginada con filtros opcionales (texto en razón social/nombre comercial, tipo, estado). */
    Mono<PagedResponse<Customer>> search(String search, Integer typeId, Integer statusId, int page, int size);

    Mono<Customer> activate(Integer customerId);

    Mono<Void> deactivate(Integer customerId);
}
