package com.vendingcom.customer_service.application.dto.response;

import com.vendingcom.customer_service.domain.model.CustomerAuditLog;

import java.time.LocalDateTime;

public record CustomerAuditLogResponse(
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
    public static CustomerAuditLogResponse fromDomain(CustomerAuditLog auditLog) {
        return new CustomerAuditLogResponse(
                auditLog.auditLogId(),
                auditLog.customerId(),
                auditLog.affectedTableName(),
                auditLog.affectedRecordId(),
                auditLog.actionType(),
                auditLog.actionDescription(),
                auditLog.oldData(),
                auditLog.newData(),
                auditLog.ipAddress(),
                auditLog.userAgent(),
                auditLog.executedByUserId(),
                auditLog.executedAt()
        );
    }
}
