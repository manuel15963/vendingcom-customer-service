package com.vendingcom.customer_service.application.service;

import com.vendingcom.customer_service.application.dto.request.CreateDocumentRequest;
import com.vendingcom.customer_service.application.dto.request.UpdateDocumentRequest;
import com.vendingcom.customer_service.application.port.input.CustomerDocumentUseCase;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerAuditLogRepositoryPort;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerDocumentRepositoryPort;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerParameterRepositoryPort;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerRepositoryPort;
import com.vendingcom.customer_service.domain.exception.BusinessRuleException;
import com.vendingcom.customer_service.domain.exception.DuplicateResourceException;
import com.vendingcom.customer_service.domain.exception.ResourceNotFoundException;
import com.vendingcom.customer_service.domain.model.CustomerAuditLog;
import com.vendingcom.customer_service.domain.model.CustomerDocument;
import com.vendingcom.customer_service.util.audit.AuditDataSerializer;
import com.vendingcom.customer_service.util.request.RequestContext;
import com.vendingcom.customer_service.util.request.RequestContextFilter;
import com.vendingcom.customer_service.util.security.JwtAuthenticationFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CustomerDocumentService implements CustomerDocumentUseCase {

    private static final String GROUP_DOCUMENT_TYPE = "DOCUMENT_TYPE";
    private static final String GROUP_DOCUMENT_STATUS = "DOCUMENT_STATUS";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_INACTIVE = "INACTIVE";
    private static final String TABLE_DOCUMENTS = "customer_documents";

    private final CustomerDocumentRepositoryPort documentRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final CustomerParameterRepositoryPort parameterRepositoryPort;
    private final CustomerAuditLogRepositoryPort auditLogRepositoryPort;

    public CustomerDocumentService(
            CustomerDocumentRepositoryPort documentRepositoryPort,
            CustomerRepositoryPort customerRepositoryPort,
            CustomerParameterRepositoryPort parameterRepositoryPort,
            CustomerAuditLogRepositoryPort auditLogRepositoryPort
    ) {
        this.documentRepositoryPort = documentRepositoryPort;
        this.customerRepositoryPort = customerRepositoryPort;
        this.parameterRepositoryPort = parameterRepositoryPort;
        this.auditLogRepositoryPort = auditLogRepositoryPort;
    }

    @Override
    @Transactional
    public Mono<CustomerDocument> create(Integer customerId, CreateDocumentRequest request) {
        String documentNumber = normalize(request.documentNumber());
        return ensureCustomerExists(customerId)
                .then(validateDocumentType(request.documentTypeId()))
                .then(checkDuplicate(request.documentTypeId(), documentNumber, null))
                .then(resolveStatusId(STATUS_ACTIVE))
                .flatMap(statusId -> currentActorId().flatMap(actor -> {
                    boolean primary = Boolean.TRUE.equals(request.isPrimary());
                    return clearPrimaryIfNeeded(customerId, primary)
                            .then(Mono.defer(() -> {
                                CustomerDocument toSave = new CustomerDocument(
                                        null,
                                        customerId,
                                        request.documentTypeId(),
                                        documentNumber,
                                        normalize(request.fileUrl()),
                                        primary,
                                        statusId,
                                        actor.orElse(null),
                                        null,
                                        null,
                                        null,
                                        null,
                                        null
                                );
                                return documentRepositoryPort.save(toSave)
                                        .flatMap(saved -> saveAudit(
                                                "DOCUMENT_CREATED", customerId, saved.documentId(), actor.orElse(null),
                                                "Documento creado para el cliente " + customerId,
                                                null, AuditDataSerializer.serialize(saved)
                                        ).then(documentRepositoryPort.findById(saved.documentId())));
                            }));
                }));
    }

    @Override
    public Flux<CustomerDocument> findByCustomer(Integer customerId) {
        return ensureCustomerExists(customerId)
                .thenMany(documentRepositoryPort.findByCustomerId(customerId));
    }

    @Override
    public Mono<CustomerDocument> findById(Integer customerId, Integer documentId) {
        return findDocumentOfCustomer(customerId, documentId);
    }

    @Override
    @Transactional
    public Mono<CustomerDocument> update(Integer customerId, Integer documentId, UpdateDocumentRequest request) {
        String documentNumber = normalize(request.documentNumber());
        return findDocumentOfCustomer(customerId, documentId)
                .flatMap(existing -> validateDocumentType(request.documentTypeId())
                        .then(checkDuplicate(request.documentTypeId(), documentNumber, documentId))
                        .then(currentActorId())
                        .flatMap(actor -> {
                            boolean primary = Boolean.TRUE.equals(request.isPrimary());
                            boolean needsClear = primary && !Boolean.TRUE.equals(existing.isPrimary());
                            return clearPrimaryIfNeeded(customerId, needsClear)
                                    .then(Mono.defer(() -> {
                                        CustomerDocument toUpdate = new CustomerDocument(
                                                existing.documentId(),
                                                existing.customerId(),
                                                request.documentTypeId(),
                                                documentNumber,
                                                normalize(request.fileUrl()),
                                                primary,
                                                existing.documentStatusId(),
                                                existing.createdByUserId(),
                                                actor.orElse(null),
                                                existing.createdAt(),
                                                LocalDateTime.now(),
                                                null,
                                                null
                                        );
                                        return documentRepositoryPort.save(toUpdate)
                                                .flatMap(updated -> saveAudit(
                                                        "DOCUMENT_UPDATED", customerId, updated.documentId(), actor.orElse(null),
                                                        "Documento actualizado del cliente " + customerId,
                                                        AuditDataSerializer.serialize(existing), AuditDataSerializer.serialize(updated)
                                                ).then(documentRepositoryPort.findById(updated.documentId())));
                                    }));
                        }));
    }

    @Override
    @Transactional
    public Mono<CustomerDocument> activate(Integer customerId, Integer documentId) {
        return changeStatus(customerId, documentId, STATUS_ACTIVE, "DOCUMENT_ACTIVATED", "Documento activado del cliente ");
    }

    @Override
    @Transactional
    public Mono<Void> deactivate(Integer customerId, Integer documentId) {
        return changeStatus(customerId, documentId, STATUS_INACTIVE, "DOCUMENT_DEACTIVATED", "Documento desactivado del cliente ").then();
    }

    private Mono<CustomerDocument> changeStatus(Integer customerId, Integer documentId, String statusCode, String action, String description) {
        return findDocumentOfCustomer(customerId, documentId)
                .flatMap(existing -> resolveStatusId(statusCode).flatMap(statusId -> {
                    if (statusId.equals(existing.documentStatusId())) {
                        return Mono.error(new BusinessRuleException(
                                "DOCUMENT_STATUS_UNCHANGED", "El documento ya se encuentra en ese estado."));
                    }
                    return currentActorId().flatMap(actor -> {
                        CustomerDocument toUpdate = new CustomerDocument(
                                existing.documentId(),
                                existing.customerId(),
                                existing.documentTypeId(),
                                existing.documentNumber(),
                                existing.fileUrl(),
                                STATUS_INACTIVE.equals(statusCode) ? Boolean.FALSE : existing.isPrimary(),
                                statusId,
                                existing.createdByUserId(),
                                actor.orElse(null),
                                existing.createdAt(),
                                LocalDateTime.now(),
                                null,
                                null
                        );
                        return documentRepositoryPort.save(toUpdate)
                                .flatMap(updated -> saveAudit(
                                        action, customerId, updated.documentId(), actor.orElse(null),
                                        description + customerId,
                                        AuditDataSerializer.serialize(existing), AuditDataSerializer.serialize(updated)
                                ).then(documentRepositoryPort.findById(updated.documentId())));
                    });
                }));
    }

    private Mono<Void> ensureCustomerExists(Integer customerId) {
        return customerRepositoryPort.findById(customerId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("No se encontró el cliente con id: " + customerId)))
                .then();
    }

    private Mono<CustomerDocument> findDocumentOfCustomer(Integer customerId, Integer documentId) {
        return documentRepositoryPort.findById(documentId)
                .filter(document -> document.customerId().equals(customerId))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "No se encontró el documento " + documentId + " para el cliente " + customerId)));
    }

    private Mono<Void> validateDocumentType(Integer documentTypeId) {
        return parameterRepositoryPort.existsByIdAndGroup(documentTypeId, GROUP_DOCUMENT_TYPE)
                .flatMap(exists -> Boolean.TRUE.equals(exists)
                        ? Mono.empty()
                        : Mono.error(new BusinessRuleException(
                        "INVALID_DOCUMENT_TYPE", "El tipo de documento indicado no es válido.")));
    }

    private Mono<Void> checkDuplicate(Integer documentTypeId, String documentNumber, Integer excludeDocumentId) {
        return documentRepositoryPort.findByTypeAndNumber(documentTypeId, documentNumber)
                .flatMap(found -> (excludeDocumentId != null && found.documentId().equals(excludeDocumentId))
                        ? Mono.<Void>empty()
                        : Mono.<Void>error(new DuplicateResourceException(
                        "Ya existe un documento registrado con ese tipo y número.")))
                .then();
    }

    private Mono<Void> clearPrimaryIfNeeded(Integer customerId, boolean primary) {
        return primary ? documentRepositoryPort.clearPrimaryFlag(customerId) : Mono.empty();
    }

    private Mono<Integer> resolveStatusId(String statusCode) {
        return parameterRepositoryPort.findIdByGroupAndCode(GROUP_DOCUMENT_STATUS, statusCode)
                .switchIfEmpty(Mono.error(new BusinessRuleException(
                        "DOCUMENT_STATUS_NOT_CONFIGURED", "No está configurado el estado de documento: " + statusCode)));
    }

    private Mono<Optional<Integer>> currentActorId() {
        return Mono.deferContextual(ctx -> Mono.just(
                ctx.hasKey(JwtAuthenticationFilter.AUTH_USER_ID_KEY)
                        ? Optional.ofNullable((Integer) ctx.get(JwtAuthenticationFilter.AUTH_USER_ID_KEY))
                        : Optional.<Integer>empty()
        ));
    }

    private Mono<CustomerAuditLog> saveAudit(
            String actionType,
            Integer customerId,
            Integer affectedRecordId,
            Integer executedByUserId,
            String description,
            String oldData,
            String newData
    ) {
        return Mono.deferContextual(ctx -> {
            String clientIp = "UNKNOWN";
            String userAgent = "UNKNOWN";
            try {
                RequestContext requestContext = (RequestContext) ctx.get(RequestContextFilter.REQUEST_CONTEXT_KEY);
                clientIp = requestContext.clientIp();
                userAgent = requestContext.userAgent();
            } catch (Exception ignored) {
                // sin contexto reactivo se usa UNKNOWN
            }

            Integer resolvedExecutedBy = executedByUserId;
            if (resolvedExecutedBy == null && ctx.hasKey(JwtAuthenticationFilter.AUTH_USER_ID_KEY)) {
                resolvedExecutedBy = (Integer) ctx.get(JwtAuthenticationFilter.AUTH_USER_ID_KEY);
            }

            CustomerAuditLog auditLog = new CustomerAuditLog(
                    null,
                    customerId,
                    TABLE_DOCUMENTS,
                    affectedRecordId,
                    actionType,
                    description,
                    oldData,
                    newData,
                    clientIp,
                    userAgent,
                    resolvedExecutedBy,
                    LocalDateTime.now()
            );

            return auditLogRepositoryPort.save(auditLog);
        });
    }

    private String normalize(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
