package com.vendingcom.customer_service.infrastructure.adapters.inbound.rest.controller;

import com.vendingcom.customer_service.application.dto.response.CustomerAuditLogResponse;
import com.vendingcom.customer_service.application.port.input.CustomerAuditLogUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/customer-audit-logs")
@Tag(name = "Auditoría", description = "Consulta del historial de cambios del módulo de clientes (solo ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class CustomerAuditLogController {

    private final CustomerAuditLogUseCase customerAuditLogUseCase;

    public CustomerAuditLogController(CustomerAuditLogUseCase customerAuditLogUseCase) {
        this.customerAuditLogUseCase = customerAuditLogUseCase;
    }

    @Operation(summary = "Listar auditoría", description = "Últimos 500 eventos de auditoría, del más reciente al más antiguo.")
    @GetMapping
    public Flux<CustomerAuditLogResponse> findAll() {
        return customerAuditLogUseCase.findAll().map(CustomerAuditLogResponse::fromDomain);
    }

    @Operation(summary = "Auditoría por cliente")
    @GetMapping("/customer/{customerId}")
    public Flux<CustomerAuditLogResponse> findByCustomer(@PathVariable Integer customerId) {
        return customerAuditLogUseCase.findByCustomer(customerId).map(CustomerAuditLogResponse::fromDomain);
    }

    @Operation(summary = "Auditoría por tipo de acción", description = "Ej: CUSTOMER_CREATED, CONTACT_UPDATED, DOCUMENT_DEACTIVATED.")
    @GetMapping("/action/{actionType}")
    public Flux<CustomerAuditLogResponse> findByActionType(@PathVariable String actionType) {
        return customerAuditLogUseCase.findByActionType(actionType).map(CustomerAuditLogResponse::fromDomain);
    }
}
