package com.vendingcom.customer_service.infrastructure.adapters.outbound.persistence.repository;

import com.vendingcom.customer_service.infrastructure.adapters.outbound.persistence.entity.CustomerContactEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveCustomerContactRepository extends ReactiveCrudRepository<CustomerContactEntity, Integer> {

    Flux<CustomerContactEntity> findByCustomerId(Integer customerId);

    @Query("UPDATE customer_contacts SET is_primary = false WHERE customer_id = :customerId AND is_primary = true")
    Mono<Void> clearPrimaryFlag(Integer customerId);
}
