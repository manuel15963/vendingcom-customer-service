package com.vendingcom.customer_service.application.port.output.persistence;

import com.vendingcom.customer_service.domain.model.CustomerAuditLog;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface CustomerAuditLogRepositoryPort {

    Mono<CustomerAuditLog> save(CustomerAuditLog auditLog);

    Flux<CustomerAuditLog> findAll();

    Flux<CustomerAuditLog> findByCustomerId(Integer customerId);

    Flux<CustomerAuditLog> findByActionType(String actionType);

    /** Elimina los registros de auditoría anteriores a la fecha indicada (retención). */
    Mono<Void> deleteOlderThan(LocalDateTime threshold);
}
