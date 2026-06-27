package com.vendingcom.customer_service.application.port.input;

import com.vendingcom.customer_service.application.dto.request.CreateContactRequest;
import com.vendingcom.customer_service.application.dto.request.UpdateContactRequest;
import com.vendingcom.customer_service.domain.model.CustomerContact;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerContactUseCase {

    Mono<CustomerContact> create(Integer customerId, CreateContactRequest request);

    Flux<CustomerContact> findByCustomer(Integer customerId);

    Mono<CustomerContact> findById(Integer customerId, Integer contactId);

    Mono<CustomerContact> update(Integer customerId, Integer contactId, UpdateContactRequest request);

    Mono<CustomerContact> activate(Integer customerId, Integer contactId);

    Mono<Void> deactivate(Integer customerId, Integer contactId);
}
