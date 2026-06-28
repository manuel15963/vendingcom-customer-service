package com.vendingcom.customer_service.application.service;

import com.vendingcom.customer_service.application.dto.request.CreateContactRequest;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerAuditLogRepositoryPort;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerContactRepositoryPort;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerParameterRepositoryPort;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerRepositoryPort;
import com.vendingcom.customer_service.domain.exception.BusinessRuleException;
import com.vendingcom.customer_service.domain.exception.ResourceNotFoundException;
import com.vendingcom.customer_service.domain.model.Customer;
import com.vendingcom.customer_service.domain.model.CustomerContact;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerContactServiceTest {

    @Mock private CustomerContactRepositoryPort contactPort;
    @Mock private CustomerRepositoryPort customerPort;
    @Mock private CustomerParameterRepositoryPort parameterPort;
    @Mock private CustomerAuditLogRepositoryPort auditPort;

    @InjectMocks private CustomerContactService service;

    private CustomerContact contact(Integer id, Integer customerId) {
        return new CustomerContact(id, customerId, "Juan", "Cargo", "j@x.com", "999",
                true, 6, 1, null, LocalDateTime.now(), null, "ACTIVO");
    }

    private Customer customer() {
        return new Customer(1, "X", null, 3, null, null, null, 1, null, null,
                LocalDateTime.now(), null, "EMPRESA", "ACTIVO");
    }

    private CreateContactRequest request() {
        return new CreateContactRequest("Juan", "Cargo", "j@x.com", "999", true);
    }

    @Test
    void create_clienteNoExiste_notFound() {
        when(customerPort.findById(1)).thenReturn(Mono.empty());
        when(parameterPort.findIdByGroupAndCode("CONTACT_STATUS", "ACTIVE")).thenReturn(Mono.just(6)); // eager

        StepVerifier.create(service.create(1, request()))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void findById_contactoDeOtroCliente_notFound() {
        when(contactPort.findById(5)).thenReturn(Mono.just(contact(5, 99)));

        StepVerifier.create(service.findById(1, 5))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void deactivate_quitaLaMarcaDePrincipal() {
        CustomerContact primaryActive = contact(5, 1); // isPrimary=true, contactStatusId=6
        when(contactPort.findById(5)).thenReturn(Mono.just(primaryActive), Mono.just(primaryActive));
        when(parameterPort.findIdByGroupAndCode("CONTACT_STATUS", "INACTIVE")).thenReturn(Mono.just(7));
        when(auditPort.save(any())).thenReturn(Mono.empty());
        ArgumentCaptor<CustomerContact> captor = ArgumentCaptor.forClass(CustomerContact.class);
        when(contactPort.save(captor.capture())).thenReturn(Mono.just(primaryActive));

        StepVerifier.create(service.deactivate(1, 5)).verifyComplete();

        assertEquals(Boolean.FALSE, captor.getValue().isPrimary());
    }

    @Test
    void create_clienteInactivo_lanzaError() {
        // No se puede agregar un contacto a un cliente inactivo.
        Customer inactive = new Customer(1, "X", null, 3, null, null, null, 2, null, null,
                LocalDateTime.now(), null, "EMPRESA", "INACTIVO");
        when(customerPort.findById(1)).thenReturn(Mono.just(inactive));
        when(parameterPort.findIdByGroupAndCode("CONTACT_STATUS", "ACTIVE")).thenReturn(Mono.just(6)); // eager

        StepVerifier.create(service.create(1, request()))
                .expectError(BusinessRuleException.class)
                .verify();
    }

    @Test
    void create_ok_desmarcaPrincipalYAudita() {
        when(customerPort.findById(1)).thenReturn(Mono.just(customer()));
        when(parameterPort.findIdByGroupAndCode("CONTACT_STATUS", "ACTIVE")).thenReturn(Mono.just(6));
        when(contactPort.clearPrimaryFlag(1)).thenReturn(Mono.empty());
        CustomerContact saved = contact(10, 1);
        when(contactPort.save(any())).thenReturn(Mono.just(saved));
        when(auditPort.save(any())).thenReturn(Mono.empty());
        when(contactPort.findById(10)).thenReturn(Mono.just(saved));

        StepVerifier.create(service.create(1, request()))
                .expectNextMatches(c -> c.contactId().equals(10) && "ACTIVO".equals(c.contactStatusName()))
                .verifyComplete();
    }
}
