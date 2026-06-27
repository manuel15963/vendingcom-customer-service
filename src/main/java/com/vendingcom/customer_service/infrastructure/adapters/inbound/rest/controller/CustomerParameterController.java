package com.vendingcom.customer_service.infrastructure.adapters.inbound.rest.controller;

import com.vendingcom.customer_service.application.dto.response.CustomerParameterResponse;
import com.vendingcom.customer_service.application.port.input.CustomerParameterUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/customer-parameters")
@Tag(name = "Catálogos", description = "Catálogos del módulo de clientes (tipos y estados) para combos del frontend")
@SecurityRequirement(name = "bearerAuth")
public class CustomerParameterController {

    private final CustomerParameterUseCase customerParameterUseCase;

    public CustomerParameterController(CustomerParameterUseCase customerParameterUseCase) {
        this.customerParameterUseCase = customerParameterUseCase;
    }

    @Operation(
            summary = "Listar catálogos",
            description = "Lista los parámetros activos. Si se envía 'group' filtra por grupo "
                    + "(ej: CUSTOMER_TYPE, CUSTOMER_STATUS, DOCUMENT_TYPE, ADDRESS_TYPE)."
    )
    @GetMapping
    public Flux<CustomerParameterResponse> find(
            @RequestParam(name = "group", required = false) String group
    ) {
        return (group == null || group.isBlank()
                ? customerParameterUseCase.findAllActive()
                : customerParameterUseCase.findByGroup(group))
                .map(CustomerParameterResponse::fromDomain);
    }
}
