package com.vendingcom.customer_service.infrastructure.adapters.inbound.rest.controller;

import com.vendingcom.customer_service.application.dto.response.CustomerDetailResponse;
import com.vendingcom.customer_service.application.port.input.CustomerDetailUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/detail")
@Tag(name = "Clientes", description = "Ficha completa del cliente")
@SecurityRequirement(name = "bearerAuth")
public class CustomerDetailController {

    private final CustomerDetailUseCase customerDetailUseCase;

    public CustomerDetailController(CustomerDetailUseCase customerDetailUseCase) {
        this.customerDetailUseCase = customerDetailUseCase;
    }

    @Operation(
            summary = "Ficha completa del cliente",
            description = "Devuelve el cliente junto con sus contactos, direcciones y documentos en una sola respuesta."
    )
    @GetMapping
    public Mono<CustomerDetailResponse> findDetail(@PathVariable Integer customerId) {
        return customerDetailUseCase.findDetail(customerId);
    }
}
