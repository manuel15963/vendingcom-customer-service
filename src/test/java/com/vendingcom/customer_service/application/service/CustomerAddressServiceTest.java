package com.vendingcom.customer_service.application.service;

import com.vendingcom.customer_service.application.dto.request.CreateAddressRequest;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerAddressRepositoryPort;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerAuditLogRepositoryPort;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerParameterRepositoryPort;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerRepositoryPort;
import com.vendingcom.customer_service.domain.exception.BusinessRuleException;
import com.vendingcom.customer_service.domain.model.Customer;
import com.vendingcom.customer_service.domain.model.CustomerAddress;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerAddressServiceTest {

    @Mock private CustomerAddressRepositoryPort addressPort;
    @Mock private CustomerRepositoryPort customerPort;
    @Mock private CustomerParameterRepositoryPort parameterPort;
    @Mock private CustomerAuditLogRepositoryPort auditPort;

    @InjectMocks private CustomerAddressService service;

    private Customer customer() {
        return new Customer(1, "X", null, 3, null, null, null, 1, null, null,
                LocalDateTime.now(), null, "EMPRESA", "ACTIVO");
    }

    private CustomerAddress address(Integer id) {
        return new CustomerAddress(id, 1, 8, "Av 1", "d", "p", "dep", "Perú", "ref",
                true, 9, 1, null, LocalDateTime.now(), null, "FISCAL", "ACTIVO");
    }

    @Test
    void create_tipoInvalido_lanzaBusinessRule() {
        when(customerPort.findById(1)).thenReturn(Mono.just(customer()));
        when(parameterPort.existsByIdAndGroup(8, "ADDRESS_TYPE")).thenReturn(Mono.just(false));
        when(parameterPort.findIdByGroupAndCode("ADDRESS_STATUS", "ACTIVE")).thenReturn(Mono.just(9)); // eager

        CreateAddressRequest req = new CreateAddressRequest(8, "Av 1", "d", "p", "dep", "Perú", "ref", true);
        StepVerifier.create(service.create(1, req))
                .expectErrorMatches(e -> e instanceof BusinessRuleException
                        && "INVALID_ADDRESS_TYPE".equals(((BusinessRuleException) e).getCode()))
                .verify();
    }

    @Test
    void create_paisPorDefectoPeru_cuandoNoSeEnvia() {
        when(customerPort.findById(1)).thenReturn(Mono.just(customer()));
        when(parameterPort.existsByIdAndGroup(8, "ADDRESS_TYPE")).thenReturn(Mono.just(true));
        when(parameterPort.findIdByGroupAndCode("ADDRESS_STATUS", "ACTIVE")).thenReturn(Mono.just(9));
        when(addressPort.clearPrimaryFlag(1)).thenReturn(Mono.empty());
        when(addressPort.save(any())).thenReturn(Mono.just(address(10)));
        when(auditPort.save(any())).thenReturn(Mono.empty());
        when(addressPort.findById(10)).thenReturn(Mono.just(address(10)));

        // país = null en el request
        CreateAddressRequest req = new CreateAddressRequest(8, "Av 1", "d", "p", "dep", null, "ref", true);
        StepVerifier.create(service.create(1, req)).expectNextCount(1).verifyComplete();

        ArgumentCaptor<CustomerAddress> captor = ArgumentCaptor.forClass(CustomerAddress.class);
        verify(addressPort).save(captor.capture());
        assertEquals("Perú", captor.getValue().country());
    }
}
