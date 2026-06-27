package com.vendingcom.customer_service.application.port.input;

import com.vendingcom.customer_service.application.dto.request.CreateAddressRequest;
import com.vendingcom.customer_service.application.dto.request.UpdateAddressRequest;
import com.vendingcom.customer_service.domain.model.CustomerAddress;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerAddressUseCase {

    Mono<CustomerAddress> create(Integer customerId, CreateAddressRequest request);

    Flux<CustomerAddress> findByCustomer(Integer customerId);

    Mono<CustomerAddress> findById(Integer customerId, Integer addressId);

    Mono<CustomerAddress> update(Integer customerId, Integer addressId, UpdateAddressRequest request);

    Mono<CustomerAddress> activate(Integer customerId, Integer addressId);

    Mono<Void> deactivate(Integer customerId, Integer addressId);
}
