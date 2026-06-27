package com.vendingcom.customer_service.infrastructure.adapters.outbound.persistence.adapter;

import com.vendingcom.customer_service.application.port.output.persistence.CustomerAuditLogRepositoryPort;
import com.vendingcom.customer_service.domain.model.CustomerAuditLog;
import io.r2dbc.spi.Row;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
public class CustomerAuditLogPersistenceAdapter implements CustomerAuditLogRepositoryPort {

    private final DatabaseClient databaseClient;

    public CustomerAuditLogPersistenceAdapter(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<CustomerAuditLog> save(CustomerAuditLog auditLog) {
        String sql = """
                INSERT INTO customer_audit_logs (
                    customer_id,
                    affected_table_name,
                    affected_record_id,
                    action_type,
                    action_description,
                    old_data,
                    new_data,
                    ip_address,
                    user_agent,
                    executed_by_user_id,
                    executed_at
                ) VALUES (
                    :customerId,
                    :affectedTableName,
                    :affectedRecordId,
                    :actionType,
                    :actionDescription,
                    CAST(:oldData AS jsonb),
                    CAST(:newData AS jsonb),
                    :ipAddress,
                    :userAgent,
                    :executedByUserId,
                    :executedAt
                )
                RETURNING
                    audit_log_id,
                    customer_id,
                    affected_table_name,
                    affected_record_id,
                    action_type,
                    action_description,
                    old_data::text AS old_data,
                    new_data::text AS new_data,
                    ip_address,
                    user_agent,
                    executed_by_user_id,
                    executed_at
                """;

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql);

        spec = auditLog.customerId() == null
                ? spec.bindNull("customerId", Integer.class)
                : spec.bind("customerId", auditLog.customerId());

        spec = auditLog.affectedTableName() == null
                ? spec.bindNull("affectedTableName", String.class)
                : spec.bind("affectedTableName", auditLog.affectedTableName());

        spec = auditLog.affectedRecordId() == null
                ? spec.bindNull("affectedRecordId", Integer.class)
                : spec.bind("affectedRecordId", auditLog.affectedRecordId());

        spec = spec.bind("actionType", auditLog.actionType());

        spec = auditLog.actionDescription() == null
                ? spec.bindNull("actionDescription", String.class)
                : spec.bind("actionDescription", auditLog.actionDescription());

        spec = auditLog.oldData() == null
                ? spec.bindNull("oldData", String.class)
                : spec.bind("oldData", auditLog.oldData());

        spec = auditLog.newData() == null
                ? spec.bindNull("newData", String.class)
                : spec.bind("newData", auditLog.newData());

        spec = auditLog.ipAddress() == null
                ? spec.bindNull("ipAddress", String.class)
                : spec.bind("ipAddress", auditLog.ipAddress());

        spec = auditLog.userAgent() == null
                ? spec.bindNull("userAgent", String.class)
                : spec.bind("userAgent", auditLog.userAgent());

        spec = auditLog.executedByUserId() == null
                ? spec.bindNull("executedByUserId", Integer.class)
                : spec.bind("executedByUserId", auditLog.executedByUserId());

        spec = auditLog.executedAt() == null
                ? spec.bind("executedAt", LocalDateTime.now())
                : spec.bind("executedAt", auditLog.executedAt());

        return spec.map((row, metadata) -> new CustomerAuditLog(
                        row.get("audit_log_id", Long.class),
                        row.get("customer_id", Integer.class),
                        row.get("affected_table_name", String.class),
                        row.get("affected_record_id", Integer.class),
                        row.get("action_type", String.class),
                        row.get("action_description", String.class),
                        row.get("old_data", String.class),
                        row.get("new_data", String.class),
                        row.get("ip_address", String.class),
                        row.get("user_agent", String.class),
                        row.get("executed_by_user_id", Integer.class),
                        row.get("executed_at", LocalDateTime.class)
                ))
                .one();
    }

    private static final String SELECT_BASE = """
            SELECT audit_log_id, customer_id, affected_table_name, affected_record_id, action_type,
                   action_description, old_data::text AS old_data, new_data::text AS new_data,
                   ip_address, user_agent, executed_by_user_id, executed_at
            FROM customer_audit_logs
            """;

    @Override
    public Flux<CustomerAuditLog> findAll() {
        return databaseClient.sql(SELECT_BASE + " ORDER BY executed_at DESC LIMIT 500")
                .map((row, metadata) -> mapRow(row))
                .all();
    }

    @Override
    public Flux<CustomerAuditLog> findByCustomerId(Integer customerId) {
        return databaseClient.sql(SELECT_BASE + " WHERE customer_id = :customerId ORDER BY executed_at DESC LIMIT 500")
                .bind("customerId", customerId)
                .map((row, metadata) -> mapRow(row))
                .all();
    }

    @Override
    public Flux<CustomerAuditLog> findByActionType(String actionType) {
        return databaseClient.sql(SELECT_BASE + " WHERE action_type = :actionType ORDER BY executed_at DESC LIMIT 500")
                .bind("actionType", actionType)
                .map((row, metadata) -> mapRow(row))
                .all();
    }

    @Override
    public Mono<Void> deleteOlderThan(LocalDateTime threshold) {
        return databaseClient.sql("DELETE FROM customer_audit_logs WHERE executed_at < :threshold")
                .bind("threshold", threshold)
                .then();
    }

    private CustomerAuditLog mapRow(Row row) {
        return new CustomerAuditLog(
                row.get("audit_log_id", Long.class),
                row.get("customer_id", Integer.class),
                row.get("affected_table_name", String.class),
                row.get("affected_record_id", Integer.class),
                row.get("action_type", String.class),
                row.get("action_description", String.class),
                row.get("old_data", String.class),
                row.get("new_data", String.class),
                row.get("ip_address", String.class),
                row.get("user_agent", String.class),
                row.get("executed_by_user_id", Integer.class),
                row.get("executed_at", LocalDateTime.class)
        );
    }
}
