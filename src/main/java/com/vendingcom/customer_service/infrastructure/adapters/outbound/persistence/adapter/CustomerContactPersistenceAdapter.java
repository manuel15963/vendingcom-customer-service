package com.vendingcom.customer_service.infrastructure.adapters.outbound.persistence.adapter;

import com.vendingcom.customer_service.application.port.output.persistence.CustomerContactRepositoryPort;
import com.vendingcom.customer_service.domain.model.CustomerContact;
import com.vendingcom.customer_service.infrastructure.adapters.outbound.persistence.mapper.CustomerContactPersistenceMapper;
import com.vendingcom.customer_service.infrastructure.adapters.outbound.persistence.repository.ReactiveCustomerContactRepository;
import com.vendingcom.customer_service.util.catalog.CatalogLabels;
import io.r2dbc.spi.Row;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
public class CustomerContactPersistenceAdapter implements CustomerContactRepositoryPort {

    private static final String SELECT_WITH_LABELS = """
            SELECT c.contact_id, c.customer_id, c.full_name, c.position, c.email, c.phone,
                   c.is_primary, c.contact_status_id, c.created_by_user_id, c.updated_by_user_id,
                   c.created_at, c.updated_at,
                   s.parameter_code AS contact_status_code
            FROM customer_contacts c
            LEFT JOIN customer_parameters s ON s.parameter_id = c.contact_status_id
            """;

    private final ReactiveCustomerContactRepository reactiveCustomerContactRepository;
    private final CustomerContactPersistenceMapper customerContactPersistenceMapper;
    private final DatabaseClient databaseClient;

    public CustomerContactPersistenceAdapter(
            ReactiveCustomerContactRepository reactiveCustomerContactRepository,
            CustomerContactPersistenceMapper customerContactPersistenceMapper,
            DatabaseClient databaseClient
    ) {
        this.reactiveCustomerContactRepository = reactiveCustomerContactRepository;
        this.customerContactPersistenceMapper = customerContactPersistenceMapper;
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<CustomerContact> save(CustomerContact contact) {
        return reactiveCustomerContactRepository.save(customerContactPersistenceMapper.toEntity(contact))
                .map(customerContactPersistenceMapper::toDomain);
    }

    @Override
    public Mono<CustomerContact> findById(Integer contactId) {
        return databaseClient.sql(SELECT_WITH_LABELS + " WHERE c.contact_id = :id")
                .bind("id", contactId)
                .map((row, metadata) -> mapRow(row))
                .one();
    }

    @Override
    public Flux<CustomerContact> findByCustomerId(Integer customerId) {
        return databaseClient.sql(SELECT_WITH_LABELS + " WHERE c.customer_id = :customerId ORDER BY c.contact_id")
                .bind("customerId", customerId)
                .map((row, metadata) -> mapRow(row))
                .all();
    }

    @Override
    public Mono<Void> clearPrimaryFlag(Integer customerId) {
        return reactiveCustomerContactRepository.clearPrimaryFlag(customerId);
    }

    private CustomerContact mapRow(Row row) {
        return new CustomerContact(
                row.get("contact_id", Integer.class),
                row.get("customer_id", Integer.class),
                row.get("full_name", String.class),
                row.get("position", String.class),
                row.get("email", String.class),
                row.get("phone", String.class),
                row.get("is_primary", Boolean.class),
                row.get("contact_status_id", Integer.class),
                row.get("created_by_user_id", Integer.class),
                row.get("updated_by_user_id", Integer.class),
                row.get("created_at", LocalDateTime.class),
                row.get("updated_at", LocalDateTime.class),
                CatalogLabels.statusLabel(row.get("contact_status_code", String.class))
        );
    }
}
