package com.vendingcom.customer_service.infrastructure.adapters.inbound.rest.controller;

import com.vendingcom.customer_service.application.dto.request.CreateCustomerRequest;
import com.vendingcom.customer_service.application.dto.request.UpdateCustomerRequest;
import com.vendingcom.customer_service.application.dto.response.CustomerResponse;
import com.vendingcom.customer_service.application.dto.response.MessageResponse;
import com.vendingcom.customer_service.application.dto.response.PagedResponse;
import com.vendingcom.customer_service.application.port.input.CustomerUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Clientes", description = "Gestión de clientes de la empresa de vending")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    private final CustomerUseCase customerUseCase;

    public CustomerController(CustomerUseCase customerUseCase) {
        this.customerUseCase = customerUseCase;
    }

    @Operation(
            summary = "Buscar / listar clientes (paginado)",
            description = "Filtros opcionales: 'search' (texto en razón social/nombre comercial), 'typeId', 'statusId'. "
                    + "Paginación con 'page' (desde 0) y 'size' (máx. 100)."
    )
    @GetMapping
    public Mono<PagedResponse<CustomerResponse>> search(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "typeId", required = false) Integer typeId,
            @RequestParam(name = "statusId", required = false) Integer statusId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        return customerUseCase.search(search, typeId, statusId, page, size)
                .map(paged -> paged.map(CustomerResponse::fromDomain));
    }

    @Operation(summary = "Buscar cliente por ID")
    @GetMapping("/{customerId}")
    public Mono<CustomerResponse> findById(@PathVariable Integer customerId) {
        return customerUseCase.findById(customerId).map(CustomerResponse::fromDomain);
    }

    @Operation(summary = "Crear cliente")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest request) {
        return customerUseCase.create(request).map(CustomerResponse::fromDomain);
    }

    @Operation(summary = "Actualizar cliente")
    @PutMapping("/{customerId}")
    public Mono<CustomerResponse> update(
            @PathVariable Integer customerId,
            @Valid @RequestBody UpdateCustomerRequest request
    ) {
        return customerUseCase.update(customerId, request).map(CustomerResponse::fromDomain);
    }

    @Operation(summary = "Activar cliente")
    @PatchMapping("/{customerId}/activate")
    public Mono<CustomerResponse> activate(@PathVariable Integer customerId) {
        return customerUseCase.activate(customerId).map(CustomerResponse::fromDomain);
    }

    @Operation(summary = "Desactivar cliente (eliminación lógica)")
    @DeleteMapping("/{customerId}")
    public Mono<MessageResponse> deactivate(@PathVariable Integer customerId) {
        return customerUseCase.deactivate(customerId)
                .thenReturn(MessageResponse.of("CUSTOMER_DEACTIVATED", "Cliente desactivado correctamente."));
    }
}
