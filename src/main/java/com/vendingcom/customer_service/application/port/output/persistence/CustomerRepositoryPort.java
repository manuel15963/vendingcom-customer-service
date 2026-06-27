package com.vendingcom.customer_service.application.port.output.persistence;

import com.vendingcom.customer_service.domain.model.Customer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerRepositoryPort {

    Mono<Customer> save(Customer customer);

    Mono<Customer> findById(Integer customerId);

    /** Búsqueda paginada con filtros opcionales (texto, tipo, estado). */
    Flux<Customer> search(String search, Integer typeId, Integer statusId, int limit, long offset);

    /** Total de resultados para los mismos filtros (para la paginación). */
    Mono<Long> countSearch(String search, Integer typeId, Integer statusId);
}
