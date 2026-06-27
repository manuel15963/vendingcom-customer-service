package com.vendingcom.customer_service.infrastructure.adapters.outbound.persistence.adapter;

import com.vendingcom.customer_service.application.port.output.persistence.CustomerAddressRepositoryPort;
import com.vendingcom.customer_service.domain.model.CustomerAddress;
import com.vendingcom.customer_service.infrastructure.adapters.outbound.persistence.mapper.CustomerAddressPersistenceMapper;
import com.vendingcom.customer_service.infrastructure.adapters.outbound.persistence.repository.ReactiveCustomerAddressRepository;
import com.vendingcom.customer_service.util.catalog.CatalogLabels;
import io.r2dbc.spi.Row;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
public class CustomerAddressPersistenceAdapter implements CustomerAddressRepositoryPort {

    private static final String SELECT_WITH_LABELS = """
            SELECT a.address_id, a.customer_id, a.address_type_id, a.address_line, a.district,
                   a.province, a.department, a.country, a.reference, a.is_primary, a.address_status_id,
                   a.created_by_user_id, a.updated_by_user_id, a.created_at, a.updated_at,
                   t.parameter_value AS address_type_name,
                   s.parameter_code  AS address_status_code
            FROM customer_addresses a
            LEFT JOIN customer_parameters t ON t.parameter_id = a.address_type_id
            LEFT JOIN customer_parameters s ON s.parameter_id = a.address_status_id
            """;

    private final ReactiveCustomerAddressRepository reactiveCustomerAddressRepository;
    private final CustomerAddressPersistenceMapper customerAddressPersistenceMapper;
    private final DatabaseClient databaseClient;

    public CustomerAddressPersistenceAdapter(
            ReactiveCustomerAddressRepository reactiveCustomerAddressRepository,
            CustomerAddressPersistenceMapper customerAddressPersistenceMapper,
            DatabaseClient databaseClient
    ) {
        this.reactiveCustomerAddressRepository = reactiveCustomerAddressRepository;
        this.customerAddressPersistenceMapper = customerAddressPersistenceMapper;
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<CustomerAddress> save(CustomerAddress address) {
        return reactiveCustomerAddressRepository.save(customerAddressPersistenceMapper.toEntity(address))
                .map(customerAddressPersistenceMapper::toDomain);
    }

    @Override
    public Mono<CustomerAddress> findById(Integer addressId) {
        return databaseClient.sql(SELECT_WITH_LABELS + " WHERE a.address_id = :id")
                .bind("id", addressId)
                .map((row, metadata) -> mapRow(row))
                .one();
    }

    @Override
    public Flux<CustomerAddress> findByCustomerId(Integer customerId) {
        return databaseClient.sql(SELECT_WITH_LABELS + " WHERE a.customer_id = :customerId ORDER BY a.address_id")
                .bind("customerId", customerId)
                .map((row, metadata) -> mapRow(row))
                .all();
    }

    @Override
    public Mono<Void> clearPrimaryFlag(Integer customerId) {
        return reactiveCustomerAddressRepository.clearPrimaryFlag(customerId);
    }

    private CustomerAddress mapRow(Row row) {
        return new CustomerAddress(
                row.get("address_id", Integer.class),
                row.get("customer_id", Integer.class),
                row.get("address_type_id", Integer.class),
                row.get("address_line", String.class),
                row.get("district", String.class),
                row.get("province", String.class),
                row.get("department", String.class),
                row.get("country", String.class),
                row.get("reference", String.class),
                row.get("is_primary", Boolean.class),
                row.get("address_status_id", Integer.class),
                row.get("created_by_user_id", Integer.class),
                row.get("updated_by_user_id", Integer.class),
                row.get("created_at", LocalDateTime.class),
                row.get("updated_at", LocalDateTime.class),
                row.get("address_type_name", String.class),
                CatalogLabels.statusLabel(row.get("address_status_code", String.class))
        );
    }
}
