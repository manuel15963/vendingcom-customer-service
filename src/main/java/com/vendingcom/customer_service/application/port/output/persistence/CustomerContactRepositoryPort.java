package com.vendingcom.customer_service.application.port.output.persistence;

import com.vendingcom.customer_service.domain.model.CustomerContact;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerContactRepositoryPort {

    Mono<CustomerContact> save(CustomerContact contact);

    Mono<CustomerContact> findById(Integer contactId);

    Flux<CustomerContact> findByCustomerId(Integer customerId);

    /** Quita la marca de principal a los contactos del cliente (para reasignar el principal). */
    Mono<Void> clearPrimaryFlag(Integer customerId);
}
