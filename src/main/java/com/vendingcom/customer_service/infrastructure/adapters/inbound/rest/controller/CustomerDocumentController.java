package com.vendingcom.customer_service.infrastructure.adapters.inbound.rest.controller;

import com.vendingcom.customer_service.application.dto.request.CreateDocumentRequest;
import com.vendingcom.customer_service.application.dto.request.UpdateDocumentRequest;
import com.vendingcom.customer_service.application.dto.response.CustomerDocumentResponse;
import com.vendingcom.customer_service.application.dto.response.MessageResponse;
import com.vendingcom.customer_service.application.port.input.CustomerDocumentUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/documents")
@Tag(name = "Documentos", description = "Documentos legales asociados a un cliente")
@SecurityRequirement(name = "bearerAuth")
public class CustomerDocumentController {

    private final CustomerDocumentUseCase customerDocumentUseCase;

    public CustomerDocumentController(CustomerDocumentUseCase customerDocumentUseCase) {
        this.customerDocumentUseCase = customerDocumentUseCase;
    }

    @Operation(summary = "Listar documentos del cliente")
    @GetMapping
    public Flux<CustomerDocumentResponse> findByCustomer(@PathVariable Integer customerId) {
        return customerDocumentUseCase.findByCustomer(customerId).map(CustomerDocumentResponse::fromDomain);
    }

    @Operation(summary = "Buscar documento por ID")
    @GetMapping("/{documentId}")
    public Mono<CustomerDocumentResponse> findById(
            @PathVariable Integer customerId,
            @PathVariable Integer documentId
    ) {
        return customerDocumentUseCase.findById(customerId, documentId).map(CustomerDocumentResponse::fromDomain);
    }

    @Operation(summary = "Registrar documento")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CustomerDocumentResponse> create(
            @PathVariable Integer customerId,
            @Valid @RequestBody CreateDocumentRequest request
    ) {
        return customerDocumentUseCase.create(customerId, request).map(CustomerDocumentResponse::fromDomain);
    }

    @Operation(summary = "Actualizar documento")
    @PutMapping("/{documentId}")
    public Mono<CustomerDocumentResponse> update(
            @PathVariable Integer customerId,
            @PathVariable Integer documentId,
            @Valid @RequestBody UpdateDocumentRequest request
    ) {
        return customerDocumentUseCase.update(customerId, documentId, request).map(CustomerDocumentResponse::fromDomain);
    }

    @Operation(summary = "Activar documento")
    @PatchMapping("/{documentId}/activate")
    public Mono<CustomerDocumentResponse> activate(
            @PathVariable Integer customerId,
            @PathVariable Integer documentId
    ) {
        return customerDocumentUseCase.activate(customerId, documentId).map(CustomerDocumentResponse::fromDomain);
    }

    @Operation(summary = "Desactivar documento (eliminación lógica)")
    @DeleteMapping("/{documentId}")
    public Mono<MessageResponse> deactivate(
            @PathVariable Integer customerId,
            @PathVariable Integer documentId
    ) {
        return customerDocumentUseCase.deactivate(customerId, documentId)
                .thenReturn(MessageResponse.of("DOCUMENT_DEACTIVATED", "Documento desactivado correctamente."));
    }
}
