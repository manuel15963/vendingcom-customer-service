package com.vendingcom.customer_service.application.port.output.persistence;

import com.vendingcom.customer_service.domain.model.CustomerAddress;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerAddressRepositoryPort {

    Mono<CustomerAddress> save(CustomerAddress address);

    Mono<CustomerAddress> findById(Integer addressId);

    Flux<CustomerAddress> findByCustomerId(Integer customerId);

    /** Quita la marca de principal a las direcciones del cliente (para reasignar la principal). */
    Mono<Void> clearPrimaryFlag(Integer customerId);
}
