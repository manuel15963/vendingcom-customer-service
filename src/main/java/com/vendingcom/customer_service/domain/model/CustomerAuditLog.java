package com.vendingcom.customer_service.domain.model;

import java.time.LocalDateTime;

/**
 * Registro de auditoría del módulo de clientes (append-only).
 */
public record CustomerAuditLog(
        Long auditLogId,
        Integer customerId,
        String affectedTableName,
        Integer affectedRecordId,
        String actionType,
        String actionDescription,
        String oldData,
        String newData,
        String ipAddress,
        String userAgent,
        Integer executedByUserId,
        LocalDateTime executedAt
) {
}
