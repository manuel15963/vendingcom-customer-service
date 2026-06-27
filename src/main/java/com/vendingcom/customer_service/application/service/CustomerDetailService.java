package com.vendingcom.customer_service.application.service;

import com.vendingcom.customer_service.application.dto.response.CustomerAddressResponse;
import com.vendingcom.customer_service.application.dto.response.CustomerContactResponse;
import com.vendingcom.customer_service.application.dto.response.CustomerDetailResponse;
import com.vendingcom.customer_service.application.dto.response.CustomerDocumentResponse;
import com.vendingcom.customer_service.application.dto.response.CustomerResponse;
import com.vendingcom.customer_service.application.port.input.CustomerDetailUseCase;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerAddressRepositoryPort;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerContactRepositoryPort;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerDocumentRepositoryPort;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerRepositoryPort;
import com.vendingcom.customer_service.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CustomerDetailService implements CustomerDetailUseCase {

    private final CustomerRepositoryPort customerRepositoryPort;
    private final CustomerContactRepositoryPort contactRepositoryPort;
    private final CustomerAddressRepositoryPort addressRepositoryPort;
    private final CustomerDocumentRepositoryPort documentRepositoryPort;

    public CustomerDetailService(
            CustomerRepositoryPort customerRepositoryPort,
            CustomerContactRepositoryPort contactRepositoryPort,
            CustomerAddressRepositoryPort addressRepositoryPort,
            CustomerDocumentRepositoryPort documentRepositoryPort
    ) {
        this.customerRepositoryPort = customerRepositoryPort;
        this.contactRepositoryPort = contactRepositoryPort;
        this.addressRepositoryPort = addressRepositoryPort;
        this.documentRepositoryPort = documentRepositoryPort;
    }

    @Override
    public Mono<CustomerDetailResponse> findDetail(Integer customerId) {
        return customerRepositoryPort.findById(customerId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("No se encontró el cliente con id: " + customerId)))
                .flatMap(customer -> Mono.zip(
                        contactRepositoryPort.findByCustomerId(customerId)
                                .map(CustomerContactResponse::fromDomain).collectList(),
                        addressRepositoryPort.findByCustomerId(customerId)
                                .map(CustomerAddressResponse::fromDomain).collectList(),
                        documentRepositoryPort.findByCustomerId(customerId)
                                .map(CustomerDocumentResponse::fromDomain).collectList()
                ).map(tuple -> new CustomerDetailResponse(
                        CustomerResponse.fromDomain(customer),
                        tuple.getT1(),
                        tuple.getT2(),
                        tuple.getT3()
                )));
    }
}
