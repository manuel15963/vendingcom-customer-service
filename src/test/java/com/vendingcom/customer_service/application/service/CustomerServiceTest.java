package com.vendingcom.customer_service.application.service;

import com.vendingcom.customer_service.application.dto.request.CreateCustomerRequest;
import com.vendingcom.customer_service.application.dto.request.UpdateCustomerRequest;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerAuditLogRepositoryPort;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerParameterRepositoryPort;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerRepositoryPort;
import com.vendingcom.customer_service.domain.exception.BusinessRuleException;
import com.vendingcom.customer_service.domain.exception.ResourceNotFoundException;
import com.vendingcom.customer_service.domain.model.Customer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    private static final String GROUP_TYPE = "CUSTOMER_TYPE";
    private static final String GROUP_STATUS = "CUSTOMER_STATUS";
    private static final Integer ACTIVE_ID = 1;
    private static final Integer INACTIVE_ID = 2;
    private static final Integer TYPE_ID = 3;

    @Mock
    private CustomerRepositoryPort customerRepositoryPort;
    @Mock
    private CustomerParameterRepositoryPort parameterRepositoryPort;
    @Mock
    private CustomerAuditLogRepositoryPort auditLogRepositoryPort;

    @InjectMocks
    private CustomerService service;

    private Customer customer(Integer id, Integer statusId) {
        return new Customer(id, "Empresa X", "X", TYPE_ID, "a@b.com", "123", "web",
                statusId, 1, null, LocalDateTime.now(), null, "EMPRESA", "ACTIVO");
    }

    private CreateCustomerRequest createRequest() {
        return new CreateCustomerRequest("Empresa X", "X", TYPE_ID, "a@b.com", "123", "web");
    }

    @Test
    void create_ok_resuelveEstadoActivoYDevuelveCliente() {
        when(parameterRepositoryPort.existsByIdAndGroup(TYPE_ID, GROUP_TYPE)).thenReturn(Mono.just(true));
        when(parameterRepositoryPort.findIdByGroupAndCode(GROUP_STATUS, "ACTIVE")).thenReturn(Mono.just(ACTIVE_ID));
        Customer saved = customer(10, ACTIVE_ID);
        when(customerRepositoryPort.save(any())).thenReturn(Mono.just(saved));
        when(auditLogRepositoryPort.save(any())).thenReturn(Mono.empty());
        when(customerRepositoryPort.findById(10)).thenReturn(Mono.just(saved));

        StepVerifier.create(service.create(createRequest()))
                .expectNextMatches(c -> c.customerId().equals(10) && "ACTIVO".equals(c.customerStatusName()))
                .verifyComplete();
    }

    @Test
    void create_tipoInvalido_lanzaBusinessRule() {
        when(parameterRepositoryPort.existsByIdAndGroup(TYPE_ID, GROUP_TYPE)).thenReturn(Mono.just(false));
        // resolveStatusId se ensambla de forma eager (Reactor); se stubea aunque no se suscriba.
        when(parameterRepositoryPort.findIdByGroupAndCode(GROUP_STATUS, "ACTIVE")).thenReturn(Mono.just(ACTIVE_ID));

        StepVerifier.create(service.create(createRequest()))
                .expectErrorMatches(e -> e instanceof BusinessRuleException
                        && "INVALID_CUSTOMER_TYPE".equals(((BusinessRuleException) e).getCode()))
                .verify();
    }

    @Test
    void create_estadoNoConfigurado_lanzaBusinessRule() {
        when(parameterRepositoryPort.existsByIdAndGroup(TYPE_ID, GROUP_TYPE)).thenReturn(Mono.just(true));
        when(parameterRepositoryPort.findIdByGroupAndCode(GROUP_STATUS, "ACTIVE")).thenReturn(Mono.empty());

        StepVerifier.create(service.create(createRequest()))
                .expectErrorMatches(e -> e instanceof BusinessRuleException
                        && "CUSTOMER_STATUS_NOT_CONFIGURED".equals(((BusinessRuleException) e).getCode()))
                .verify();
    }

    @Test
    void findById_noExiste_lanzaNotFound() {
        when(customerRepositoryPort.findById(99)).thenReturn(Mono.empty());

        StepVerifier.create(service.findById(99))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void update_noExiste_lanzaNotFound() {
        when(customerRepositoryPort.findById(99)).thenReturn(Mono.empty());
        UpdateCustomerRequest req = new UpdateCustomerRequest("Empresa X", "X", TYPE_ID, "a@b.com", "123", "web");

        StepVerifier.create(service.update(99, req))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void search_devuelvePaginado() {
        when(customerRepositoryPort.search(any(), any(), any(), eq(20), eq(0L)))
                .thenReturn(Flux.just(customer(1, ACTIVE_ID), customer(2, ACTIVE_ID)));
        when(customerRepositoryPort.countSearch(any(), any(), any())).thenReturn(Mono.just(2L));

        StepVerifier.create(service.search(null, null, null, 0, 20))
                .expectNextMatches(page -> page.content().size() == 2
                        && page.totalElements() == 2 && page.totalPages() == 1)
                .verifyComplete();
    }

    @Test
    void deactivate_yaInactivo_lanzaUnchanged() {
        when(customerRepositoryPort.findById(5)).thenReturn(Mono.just(customer(5, INACTIVE_ID)));
        when(parameterRepositoryPort.findIdByGroupAndCode(GROUP_STATUS, "INACTIVE")).thenReturn(Mono.just(INACTIVE_ID));

        StepVerifier.create(service.deactivate(5))
                .expectErrorMatches(e -> e instanceof BusinessRuleException
                        && "CUSTOMER_STATUS_UNCHANGED".equals(((BusinessRuleException) e).getCode()))
                .verify();
    }

    @Test
    void deactivate_ok_cambiaEstadoYAudita() {
        // 1ra llamada (inicio) → activo; 2da (re-consulta tras guardar) → inactivo
        when(customerRepositoryPort.findById(5))
                .thenReturn(Mono.just(customer(5, ACTIVE_ID)), Mono.just(customer(5, INACTIVE_ID)));
        when(parameterRepositoryPort.findIdByGroupAndCode(GROUP_STATUS, "INACTIVE")).thenReturn(Mono.just(INACTIVE_ID));
        when(customerRepositoryPort.save(any())).thenReturn(Mono.just(customer(5, INACTIVE_ID)));
        when(auditLogRepositoryPort.save(any())).thenReturn(Mono.empty());

        StepVerifier.create(service.deactivate(5)).verifyComplete();
    }
}
