package com.vendingcom.customer_service.application.port.output.persistence;

import com.vendingcom.customer_service.domain.model.CustomerDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerDocumentRepositoryPort {

    Mono<CustomerDocument> save(CustomerDocument document);

    Mono<CustomerDocument> findById(Integer documentId);

    Flux<CustomerDocument> findByCustomerId(Integer customerId);

    Mono<Void> clearPrimaryFlag(Integer customerId);

    /** Busca un documento por tipo y número (la combinación es única en todo el sistema). */
    Mono<CustomerDocument> findByTypeAndNumber(Integer documentTypeId, String documentNumber);
}
