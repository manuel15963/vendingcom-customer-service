package com.vendingcom.customer_service.infrastructure.adapters.outbound.persistence.adapter;

import com.vendingcom.customer_service.application.port.output.persistence.CustomerParameterRepositoryPort;
import com.vendingcom.customer_service.domain.model.CustomerParameter;
import io.r2dbc.spi.Row;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class CustomerParameterPersistenceAdapter implements CustomerParameterRepositoryPort {

    private final DatabaseClient databaseClient;

    public CustomerParameterPersistenceAdapter(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Integer> findIdByGroupAndCode(String parameterGroup, String parameterCode) {
        return databaseClient.sql("""
                        SELECT parameter_id
                        FROM customer_parameters
                        WHERE parameter_group = :group
                          AND parameter_code  = :code
                          AND parameter_status = 1
                        """)
                .bind("group", parameterGroup)
                .bind("code", parameterCode)
                .map((row, metadata) -> row.get("parameter_id", Integer.class))
                .one();
    }

    @Override
    public Mono<Boolean> existsByIdAndGroup(Integer parameterId, String parameterGroup) {
        return databaseClient.sql("""
                        SELECT EXISTS (
                            SELECT 1 FROM customer_parameters
                            WHERE parameter_id = :id AND parameter_group = :group AND parameter_status = 1
                        ) AS exists
                        """)
                .bind("id", parameterId)
                .bind("group", parameterGroup)
                .map((row, metadata) -> row.get("exists", Boolean.class))
                .one();
    }

    @Override
    public Flux<CustomerParameter> findActiveByGroup(String parameterGroup) {
        return databaseClient.sql("""
                        SELECT parameter_id, parameter_group, parameter_code, parameter_value,
                               description, sort_order, parameter_status
                        FROM customer_parameters
                        WHERE parameter_group = :group AND parameter_status = 1
                        ORDER BY sort_order
                        """)
                .bind("group", parameterGroup)
                .map((row, metadata) -> toDomain(row))
                .all();
    }

    @Override
    public Flux<CustomerParameter> findAllActive() {
        return databaseClient.sql("""
                        SELECT parameter_id, parameter_group, parameter_code, parameter_value,
                               description, sort_order, parameter_status
                        FROM customer_parameters
                        WHERE parameter_status = 1
                        ORDER BY parameter_group, sort_order
                        """)
                .map((row, metadata) -> toDomain(row))
                .all();
    }

    private CustomerParameter toDomain(Row row) {
        return new CustomerParameter(
                row.get("parameter_id", Integer.class),
                row.get("parameter_group", String.class),
                row.get("parameter_code", String.class),
                row.get("parameter_value", String.class),
                row.get("description", String.class),
                row.get("sort_order", Integer.class),
                row.get("parameter_status", Integer.class)
        );
    }
}
