package com.vendingcom.customer_service.application.service;

import com.vendingcom.customer_service.application.port.input.CustomerAuditLogUseCase;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerAuditLogRepositoryPort;
import com.vendingcom.customer_service.domain.model.CustomerAuditLog;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class CustomerAuditLogService implements CustomerAuditLogUseCase {

    private final CustomerAuditLogRepositoryPort auditLogRepositoryPort;

    public CustomerAuditLogService(CustomerAuditLogRepositoryPort auditLogRepositoryPort) {
        this.auditLogRepositoryPort = auditLogRepositoryPort;
    }

    @Override
    public Flux<CustomerAuditLog> findAll() {
        return auditLogRepositoryPort.findAll();
    }

    @Override
    public Flux<CustomerAuditLog> findByCustomer(Integer customerId) {
        return auditLogRepositoryPort.findByCustomerId(customerId);
    }

    @Override
    public Flux<CustomerAuditLog> findByActionType(String actionType) {
        String normalized = actionType == null ? null : actionType.trim().toUpperCase();
        return auditLogRepositoryPort.findByActionType(normalized);
    }
}
