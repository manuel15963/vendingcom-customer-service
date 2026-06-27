package com.vendingcom.customer_service.infrastructure.adapters.outbound.persistence.adapter;

import com.vendingcom.customer_service.application.port.output.persistence.CustomerRepositoryPort;
import com.vendingcom.customer_service.domain.model.Customer;
import com.vendingcom.customer_service.util.catalog.CatalogLabels;
import com.vendingcom.customer_service.infrastructure.adapters.outbound.persistence.mapper.CustomerPersistenceMapper;
import com.vendingcom.customer_service.infrastructure.adapters.outbound.persistence.repository.ReactiveCustomerRepository;
import io.r2dbc.spi.Row;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
public class CustomerPersistenceAdapter implements CustomerRepositoryPort {

    // Lectura con JOIN al catálogo para traer las etiquetas legibles (tipo y estado).
    private static final String SELECT_WITH_LABELS = """
            SELECT c.customer_id, c.business_name, c.trade_name, c.customer_type_id, c.main_email,
                   c.main_phone, c.website, c.customer_status_id, c.created_by_user_id,
                   c.updated_by_user_id, c.created_at, c.updated_at,
                   t.parameter_value AS customer_type_name,
                   s.parameter_code  AS customer_status_code
            FROM customers c
            LEFT JOIN customer_parameters t ON t.parameter_id = c.customer_type_id
            LEFT JOIN customer_parameters s ON s.parameter_id = c.customer_status_id
            """;

    private final ReactiveCustomerRepository reactiveCustomerRepository;
    private final CustomerPersistenceMapper customerPersistenceMapper;
    private final DatabaseClient databaseClient;

    public CustomerPersistenceAdapter(
            ReactiveCustomerRepository reactiveCustomerRepository,
            CustomerPersistenceMapper customerPersistenceMapper,
            DatabaseClient databaseClient
    ) {
        this.reactiveCustomerRepository = reactiveCustomerRepository;
        this.customerPersistenceMapper = customerPersistenceMapper;
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Customer> save(Customer customer) {
        return reactiveCustomerRepository.save(customerPersistenceMapper.toEntity(customer))
                .map(customerPersistenceMapper::toDomain);
    }

    @Override
    public Mono<Customer> findById(Integer customerId) {
        return databaseClient.sql(SELECT_WITH_LABELS + " WHERE c.customer_id = :id")
                .bind("id", customerId)
                .map((row, metadata) -> mapRow(row))
                .one();
    }

    @Override
    public Flux<Customer> search(String search, Integer typeId, Integer statusId, int limit, long offset) {
        StringBuilder sql = new StringBuilder(SELECT_WITH_LABELS + " WHERE 1 = 1");
        appendFilters(sql, search, typeId, statusId);
        sql.append(" ORDER BY c.customer_id DESC LIMIT :limit OFFSET :offset");

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql.toString());
        spec = bindFilters(spec, search, typeId, statusId);
        spec = spec.bind("limit", limit).bind("offset", offset);

        return spec.map((row, metadata) -> mapRow(row)).all();
    }

    @Override
    public Mono<Long> countSearch(String search, Integer typeId, Integer statusId) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS total FROM customers c WHERE 1 = 1");
        appendFilters(sql, search, typeId, statusId);

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql.toString());
        spec = bindFilters(spec, search, typeId, statusId);

        return spec.map((row, metadata) -> row.get("total", Long.class)).one();
    }

    private void appendFilters(StringBuilder sql, String search, Integer typeId, Integer statusId) {
        if (search != null) {
            sql.append(" AND (LOWER(c.business_name) LIKE :search OR LOWER(COALESCE(c.trade_name, '')) LIKE :search)");
        }
        if (typeId != null) {
            sql.append(" AND c.customer_type_id = :typeId");
        }
        if (statusId != null) {
            sql.append(" AND c.customer_status_id = :statusId");
        }
    }

    private DatabaseClient.GenericExecuteSpec bindFilters(
            DatabaseClient.GenericExecuteSpec spec, String search, Integer typeId, Integer statusId) {
        if (search != null) {
            spec = spec.bind("search", "%" + search.toLowerCase() + "%");
        }
        if (typeId != null) {
            spec = spec.bind("typeId", typeId);
        }
        if (statusId != null) {
            spec = spec.bind("statusId", statusId);
        }
        return spec;
    }

    private Customer mapRow(Row row) {
        return new Customer(
                row.get("customer_id", Integer.class),
                row.get("business_name", String.class),
                row.get("trade_name", String.class),
                row.get("customer_type_id", Integer.class),
                row.get("main_email", String.class),
                row.get("main_phone", String.class),
                row.get("website", String.class),
                row.get("customer_status_id", Integer.class),
                row.get("created_by_user_id", Integer.class),
                row.get("updated_by_user_id", Integer.class),
                row.get("created_at", LocalDateTime.class),
                row.get("updated_at", LocalDateTime.class),
                row.get("customer_type_name", String.class),
                CatalogLabels.statusLabel(row.get("customer_status_code", String.class))
        );
    }
}
