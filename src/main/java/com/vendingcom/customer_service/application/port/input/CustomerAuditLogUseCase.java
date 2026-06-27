package com.vendingcom.customer_service.application.port.input;

import com.vendingcom.customer_service.domain.model.CustomerAuditLog;
import reactor.core.publisher.Flux;

public interface CustomerAuditLogUseCase {

    Flux<CustomerAuditLog> findAll();

    Flux<CustomerAuditLog> findByCustomer(Integer customerId);

    Flux<CustomerAuditLog> findByActionType(String actionType);
}
