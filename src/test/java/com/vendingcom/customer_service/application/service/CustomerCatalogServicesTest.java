package com.vendingcom.customer_service.application.service;

import com.vendingcom.customer_service.application.port.output.persistence.CustomerAuditLogRepositoryPort;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerParameterRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifica la normalización de entradas en los servicios de catálogo y auditoría.
 */
@ExtendWith(MockitoExtension.class)
class CustomerCatalogServicesTest {

    @Mock private CustomerParameterRepositoryPort parameterPort;
    @Mock private CustomerAuditLogRepositoryPort auditPort;

    @InjectMocks private CustomerParameterService parameterService;
    @InjectMocks private CustomerAuditLogService auditService;

    @Test
    void parametros_findByGroup_normalizaAMayusculas() {
        when(parameterPort.findActiveByGroup("CUSTOMER_TYPE")).thenReturn(Flux.empty());

        StepVerifier.create(parameterService.findByGroup("customer_type")).verifyComplete();

        verify(parameterPort).findActiveByGroup("CUSTOMER_TYPE");
    }

    @Test
    void auditoria_findByActionType_normalizaTrimYMayusculas() {
        when(auditPort.findByActionType("CUSTOMER_CREATED")).thenReturn(Flux.empty());

        StepVerifier.create(auditService.findByActionType("  customer_created  ")).verifyComplete();

        verify(auditPort).findByActionType("CUSTOMER_CREATED");
    }
}
