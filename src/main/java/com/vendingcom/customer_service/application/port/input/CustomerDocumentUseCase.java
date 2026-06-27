package com.vendingcom.customer_service.application.port.input;

import com.vendingcom.customer_service.application.dto.request.CreateDocumentRequest;
import com.vendingcom.customer_service.application.dto.request.UpdateDocumentRequest;
import com.vendingcom.customer_service.domain.model.CustomerDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerDocumentUseCase {

    Mono<CustomerDocument> create(Integer customerId, CreateDocumentRequest request);

    Flux<CustomerDocument> findByCustomer(Integer customerId);

    Mono<CustomerDocument> findById(Integer customerId, Integer documentId);

    Mono<CustomerDocument> update(Integer customerId, Integer documentId, UpdateDocumentRequest request);

    Mono<CustomerDocument> activate(Integer customerId, Integer documentId);

    Mono<Void> deactivate(Integer customerId, Integer documentId);
}
