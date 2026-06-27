package com.vendingcom.customer_service.application.service;

import com.vendingcom.customer_service.application.dto.request.CreateDocumentRequest;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerAuditLogRepositoryPort;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerDocumentRepositoryPort;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerParameterRepositoryPort;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerRepositoryPort;
import com.vendingcom.customer_service.domain.exception.DuplicateResourceException;
import com.vendingcom.customer_service.domain.model.Customer;
import com.vendingcom.customer_service.domain.model.CustomerDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerDocumentServiceTest {

    @Mock private CustomerDocumentRepositoryPort documentPort;
    @Mock private CustomerRepositoryPort customerPort;
    @Mock private CustomerParameterRepositoryPort parameterPort;
    @Mock private CustomerAuditLogRepositoryPort auditPort;

    @InjectMocks private CustomerDocumentService service;

    private Customer customer() {
        return new Customer(1, "X", null, 3, null, null, null, 1, null, null,
                LocalDateTime.now(), null, "EMPRESA", "ACTIVO");
    }

    private CustomerDocument doc(Integer id) {
        return new CustomerDocument(id, 1, 13, "20999", null, true, 7, 1, null,
                LocalDateTime.now(), null, "RUC", "ACTIVO");
    }

    private CreateDocumentRequest request() {
        return new CreateDocumentRequest(13, "20999", null, true);
    }

    @Test
    void create_documentoDuplicado_lanzaDuplicate() {
        when(customerPort.findById(1)).thenReturn(Mono.just(customer()));
        when(parameterPort.existsByIdAndGroup(13, "DOCUMENT_TYPE")).thenReturn(Mono.just(true));
        when(parameterPort.findIdByGroupAndCode("DOCUMENT_STATUS", "ACTIVE")).thenReturn(Mono.just(7)); // eager
        when(documentPort.findByTypeAndNumber(13, "20999")).thenReturn(Mono.just(doc(5)));

        StepVerifier.create(service.create(1, request()))
                .expectError(DuplicateResourceException.class)
                .verify();
    }

    @Test
    void create_ok_sinDuplicado() {
        when(customerPort.findById(1)).thenReturn(Mono.just(customer()));
        when(parameterPort.existsByIdAndGroup(13, "DOCUMENT_TYPE")).thenReturn(Mono.just(true));
        when(parameterPort.findIdByGroupAndCode("DOCUMENT_STATUS", "ACTIVE")).thenReturn(Mono.just(7));
        when(documentPort.findByTypeAndNumber(13, "20999")).thenReturn(Mono.empty());
        when(documentPort.clearPrimaryFlag(1)).thenReturn(Mono.empty());
        CustomerDocument saved = doc(10);
        when(documentPort.save(any())).thenReturn(Mono.just(saved));
        when(auditPort.save(any())).thenReturn(Mono.empty());
        when(documentPort.findById(10)).thenReturn(Mono.just(saved));

        StepVerifier.create(service.create(1, request()))
                .expectNextMatches(d -> d.documentId().equals(10) && "ACTIVO".equals(d.documentStatusName()))
                .verifyComplete();
    }
}
