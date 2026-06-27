package com.vendingcom.customer_service.application.service;

import com.vendingcom.customer_service.application.port.input.CustomerParameterUseCase;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerParameterRepositoryPort;
import com.vendingcom.customer_service.domain.model.CustomerParameter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class CustomerParameterService implements CustomerParameterUseCase {

    private final CustomerParameterRepositoryPort customerParameterRepositoryPort;

    public CustomerParameterService(CustomerParameterRepositoryPort customerParameterRepositoryPort) {
        this.customerParameterRepositoryPort = customerParameterRepositoryPort;
    }

    @Override
    public Flux<CustomerParameter> findByGroup(String parameterGroup) {
        String normalizedGroup = parameterGroup == null ? null : parameterGroup.trim().toUpperCase();
        return customerParameterRepositoryPort.findActiveByGroup(normalizedGroup);
    }

    @Override
    public Flux<CustomerParameter> findAllActive() {
        return customerParameterRepositoryPort.findAllActive();
    }
}
