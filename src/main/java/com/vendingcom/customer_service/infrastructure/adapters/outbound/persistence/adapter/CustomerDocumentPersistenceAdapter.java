package com.vendingcom.customer_service.infrastructure.adapters.outbound.persistence.adapter;

import com.vendingcom.customer_service.application.port.output.persistence.CustomerDocumentRepositoryPort;
import com.vendingcom.customer_service.domain.model.CustomerDocument;
import com.vendingcom.customer_service.infrastructure.adapters.outbound.persistence.mapper.CustomerDocumentPersistenceMapper;
import com.vendingcom.customer_service.infrastructure.adapters.outbound.persistence.repository.ReactiveCustomerDocumentRepository;
import com.vendingcom.customer_service.util.catalog.CatalogLabels;
import io.r2dbc.spi.Row;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
public class CustomerDocumentPersistenceAdapter implements CustomerDocumentRepositoryPort {

    private static final String SELECT_WITH_LABELS = """
            SELECT d.document_id, d.customer_id, d.document_type_id, d.document_number, d.file_url,
                   d.is_primary, d.document_status_id, d.created_by_user_id, d.updated_by_user_id,
                   d.created_at, d.updated_at,
                   t.parameter_value AS document_type_name,
                   s.parameter_code  AS document_status_code
            FROM customer_documents d
            LEFT JOIN customer_parameters t ON t.parameter_id = d.document_type_id
            LEFT JOIN customer_parameters s ON s.parameter_id = d.document_status_id
            """;

    private final ReactiveCustomerDocumentRepository reactiveCustomerDocumentRepository;
    private final CustomerDocumentPersistenceMapper customerDocumentPersistenceMapper;
    private final DatabaseClient databaseClient;

    public CustomerDocumentPersistenceAdapter(
            ReactiveCustomerDocumentRepository reactiveCustomerDocumentRepository,
            CustomerDocumentPersistenceMapper customerDocumentPersistenceMapper,
            DatabaseClient databaseClient
    ) {
        this.reactiveCustomerDocumentRepository = reactiveCustomerDocumentRepository;
        this.customerDocumentPersistenceMapper = customerDocumentPersistenceMapper;
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<CustomerDocument> save(CustomerDocument document) {
        return reactiveCustomerDocumentRepository.save(customerDocumentPersistenceMapper.toEntity(document))
                .map(customerDocumentPersistenceMapper::toDomain);
    }

    @Override
    public Mono<CustomerDocument> findById(Integer documentId) {
        return databaseClient.sql(SELECT_WITH_LABELS + " WHERE d.document_id = :id")
                .bind("id", documentId)
                .map((row, metadata) -> mapRow(row))
                .one();
    }

    @Override
    public Flux<CustomerDocument> findByCustomerId(Integer customerId) {
        return databaseClient.sql(SELECT_WITH_LABELS + " WHERE d.customer_id = :customerId ORDER BY d.document_id")
                .bind("customerId", customerId)
                .map((row, metadata) -> mapRow(row))
                .all();
    }

    @Override
    public Mono<Void> clearPrimaryFlag(Integer customerId) {
        return reactiveCustomerDocumentRepository.clearPrimaryFlag(customerId);
    }

    @Override
    public Mono<CustomerDocument> findByTypeAndNumber(Integer documentTypeId, String documentNumber) {
        return reactiveCustomerDocumentRepository.findByDocumentTypeIdAndDocumentNumber(documentTypeId, documentNumber)
                .map(customerDocumentPersistenceMapper::toDomain);
    }

    private CustomerDocument mapRow(Row row) {
        return new CustomerDocument(
                row.get("document_id", Integer.class),
                row.get("customer_id", Integer.class),
                row.get("document_type_id", Integer.class),
                row.get("document_number", String.class),
                row.get("file_url", String.class),
                row.get("is_primary", Boolean.class),
                row.get("document_status_id", Integer.class),
                row.get("created_by_user_id", Integer.class),
                row.get("updated_by_user_id", Integer.class),
                row.get("created_at", LocalDateTime.class),
                row.get("updated_at", LocalDateTime.class),
                row.get("document_type_name", String.class),
                CatalogLabels.statusLabel(row.get("document_status_code", String.class))
        );
    }
}
