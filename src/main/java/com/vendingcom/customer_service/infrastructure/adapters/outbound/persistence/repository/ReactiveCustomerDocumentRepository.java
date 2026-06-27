package com.vendingcom.customer_service.infrastructure.adapters.outbound.persistence.repository;

import com.vendingcom.customer_service.infrastructure.adapters.outbound.persistence.entity.CustomerDocumentEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveCustomerDocumentRepository extends ReactiveCrudRepository<CustomerDocumentEntity, Integer> {

    Flux<CustomerDocumentEntity> findByCustomerId(Integer customerId);

    Mono<CustomerDocumentEntity> findByDocumentTypeIdAndDocumentNumber(Integer documentTypeId, String documentNumber);

    @Query("UPDATE customer_documents SET is_primary = false WHERE customer_id = :customerId AND is_primary = true")
    Mono<Void> clearPrimaryFlag(Integer customerId);
}
