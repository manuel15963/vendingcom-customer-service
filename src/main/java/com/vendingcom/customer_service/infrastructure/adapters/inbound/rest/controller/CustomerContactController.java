package com.vendingcom.customer_service.infrastructure.adapters.inbound.rest.controller;

import com.vendingcom.customer_service.application.dto.request.CreateContactRequest;
import com.vendingcom.customer_service.application.dto.request.UpdateContactRequest;
import com.vendingcom.customer_service.application.dto.response.CustomerContactResponse;
import com.vendingcom.customer_service.application.dto.response.MessageResponse;
import com.vendingcom.customer_service.application.port.input.CustomerContactUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/contacts")
@Tag(name = "Contactos", description = "Contactos asociados a un cliente")
@SecurityRequirement(name = "bearerAuth")
public class CustomerContactController {

    private final CustomerContactUseCase customerContactUseCase;

    public CustomerContactController(CustomerContactUseCase customerContactUseCase) {
        this.customerContactUseCase = customerContactUseCase;
    }

    @Operation(summary = "Listar contactos del cliente")
    @GetMapping
    public Flux<CustomerContactResponse> findByCustomer(@PathVariable Integer customerId) {
        return customerContactUseCase.findByCustomer(customerId).map(CustomerContactResponse::fromDomain);
    }

    @Operation(summary = "Buscar contacto por ID")
    @GetMapping("/{contactId}")
    public Mono<CustomerContactResponse> findById(
            @PathVariable Integer customerId,
            @PathVariable Integer contactId
    ) {
        return customerContactUseCase.findById(customerId, contactId).map(CustomerContactResponse::fromDomain);
    }

    @Operation(summary = "Crear contacto")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CustomerContactResponse> create(
            @PathVariable Integer customerId,
            @Valid @RequestBody CreateContactRequest request
    ) {
        return customerContactUseCase.create(customerId, request).map(CustomerContactResponse::fromDomain);
    }

    @Operation(summary = "Actualizar contacto")
    @PutMapping("/{contactId}")
    public Mono<CustomerContactResponse> update(
            @PathVariable Integer customerId,
            @PathVariable Integer contactId,
            @Valid @RequestBody UpdateContactRequest request
    ) {
        return customerContactUseCase.update(customerId, contactId, request).map(CustomerContactResponse::fromDomain);
    }

    @Operation(summary = "Activar contacto")
    @PatchMapping("/{contactId}/activate")
    public Mono<CustomerContactResponse> activate(
            @PathVariable Integer customerId,
            @PathVariable Integer contactId
    ) {
        return customerContactUseCase.activate(customerId, contactId).map(CustomerContactResponse::fromDomain);
    }

    @Operation(summary = "Desactivar contacto (eliminación lógica)")
    @DeleteMapping("/{contactId}")
    public Mono<MessageResponse> deactivate(
            @PathVariable Integer customerId,
            @PathVariable Integer contactId
    ) {
        return customerContactUseCase.deactivate(customerId, contactId)
                .thenReturn(MessageResponse.of("CONTACT_DEACTIVATED", "Contacto desactivado correctamente."));
    }
}
