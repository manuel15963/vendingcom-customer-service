package com.vendingcom.customer_service.infrastructure.adapters.inbound.rest.controller;

import com.vendingcom.customer_service.application.dto.request.CreateAddressRequest;
import com.vendingcom.customer_service.application.dto.request.UpdateAddressRequest;
import com.vendingcom.customer_service.application.dto.response.CustomerAddressResponse;
import com.vendingcom.customer_service.application.dto.response.MessageResponse;
import com.vendingcom.customer_service.application.port.input.CustomerAddressUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/addresses")
@Tag(name = "Direcciones", description = "Direcciones asociadas a un cliente")
@SecurityRequirement(name = "bearerAuth")
public class CustomerAddressController {

    private final CustomerAddressUseCase customerAddressUseCase;

    public CustomerAddressController(CustomerAddressUseCase customerAddressUseCase) {
        this.customerAddressUseCase = customerAddressUseCase;
    }

    @Operation(summary = "Listar direcciones del cliente")
    @GetMapping
    public Flux<CustomerAddressResponse> findByCustomer(@PathVariable Integer customerId) {
        return customerAddressUseCase.findByCustomer(customerId).map(CustomerAddressResponse::fromDomain);
    }

    @Operation(summary = "Buscar dirección por ID")
    @GetMapping("/{addressId}")
    public Mono<CustomerAddressResponse> findById(
            @PathVariable Integer customerId,
            @PathVariable Integer addressId
    ) {
        return customerAddressUseCase.findById(customerId, addressId).map(CustomerAddressResponse::fromDomain);
    }

    @Operation(summary = "Crear dirección")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CustomerAddressResponse> create(
            @PathVariable Integer customerId,
            @Valid @RequestBody CreateAddressRequest request
    ) {
        return customerAddressUseCase.create(customerId, request).map(CustomerAddressResponse::fromDomain);
    }

    @Operation(summary = "Actualizar dirección")
    @PutMapping("/{addressId}")
    public Mono<CustomerAddressResponse> update(
            @PathVariable Integer customerId,
            @PathVariable Integer addressId,
            @Valid @RequestBody UpdateAddressRequest request
    ) {
        return customerAddressUseCase.update(customerId, addressId, request).map(CustomerAddressResponse::fromDomain);
    }

    @Operation(summary = "Activar dirección")
    @PatchMapping("/{addressId}/activate")
    public Mono<CustomerAddressResponse> activate(
            @PathVariable Integer customerId,
            @PathVariable Integer addressId
    ) {
        return customerAddressUseCase.activate(customerId, addressId).map(CustomerAddressResponse::fromDomain);
    }

    @Operation(summary = "Desactivar dirección (eliminación lógica)")
    @DeleteMapping("/{addressId}")
    public Mono<MessageResponse> deactivate(
            @PathVariable Integer customerId,
            @PathVariable Integer addressId
    ) {
        return customerAddressUseCase.deactivate(customerId, addressId)
                .thenReturn(MessageResponse.of("ADDRESS_DEACTIVATED", "Dirección desactivada correctamente."));
    }
}
