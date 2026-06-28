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
import java.util.Set;

@Service
public class CustomerDocumentService implements CustomerDocumentUseCase {

    private static final String GROUP_DOCUMENT_TYPE = "DOCUMENT_TYPE";
    private static final String GROUP_DOCUMENT_STATUS = "DOCUMENT_STATUS";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_INACTIVE = "INACTIVE";
    private static final String TABLE_DOCUMENTS = "customer_documents";

    // Reglas: una PERSONA usa DNI/CE/Pasaporte; una EMPRESA/INSTITUCIÓN usa RUC.
    private static final String CUSTOMER_TYPE_PERSONA = "PERSONA";
    private static final String DOC_RUC = "RUC";
    private static final Set<String> PERSONAL_DOC_CODES = Set.of("DNI", "CE", "PASAPORTE");

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
        String documentNumber = normalizeDocumentNumber(request.documentNumber());
        return ensureCustomerActive(customerId)
                .then(validateDocumentType(request.documentTypeId()))
                .then(validateDocumentNumberFormat(request.documentTypeId(), documentNumber))
                .then(validateDocumentMatchesCustomer(customerId, request.documentTypeId()))
                .then(validateSingleDocumentPerType(customerId, request.documentTypeId(), null))
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
        String documentNumber = normalizeDocumentNumber(request.documentNumber());
        return findDocumentOfCustomer(customerId, documentId)
                .flatMap(existing -> validateDocumentType(request.documentTypeId())
                        .then(validateDocumentNumberFormat(request.documentTypeId(), documentNumber))
                        .then(validateDocumentMatchesCustomer(customerId, request.documentTypeId()))
                        .then(validateSingleDocumentPerType(customerId, request.documentTypeId(), documentId))
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

    /** Para agregar registros el cliente debe existir Y estar activo. */
    private Mono<Void> ensureCustomerActive(Integer customerId) {
        return customerRepositoryPort.findById(customerId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("No se encontró el cliente con id: " + customerId)))
                .flatMap(customer -> "ACTIVO".equals(customer.customerStatusName())
                        ? Mono.<Void>empty()
                        : Mono.<Void>error(new BusinessRuleException(
                        "CUSTOMER_INACTIVE", "No se pueden agregar registros a un cliente inactivo.")));
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

    /**
     * El tipo de documento debe corresponder al tipo de cliente:
     * PERSONA -> DNI / CE / Pasaporte; EMPRESA o INSTITUCIÓN -> RUC.
     */
    private Mono<Void> validateDocumentMatchesCustomer(Integer customerId, Integer documentTypeId) {
        return customerRepositoryPort.findById(customerId)
                .flatMap(customer -> Mono.zip(
                        parameterRepositoryPort.findCodeById(customer.customerTypeId()),
                        parameterRepositoryPort.findCodeById(documentTypeId)))
                .flatMap(codes -> {
                    boolean isPerson = CUSTOMER_TYPE_PERSONA.equals(codes.getT1());
                    String docCode = codes.getT2();
                    if (isPerson && !PERSONAL_DOC_CODES.contains(docCode)) {
                        return Mono.<Void>error(new BusinessRuleException("DOCUMENT_TYPE_MISMATCH",
                                "Un cliente tipo PERSONA solo admite DNI, Carnet de Extranjería o Pasaporte."));
                    }
                    if (!isPerson && !DOC_RUC.equals(docCode)) {
                        return Mono.<Void>error(new BusinessRuleException("DOCUMENT_TYPE_MISMATCH",
                                "Un cliente tipo EMPRESA o INSTITUCIÓN solo admite RUC."));
                    }
                    return Mono.<Void>empty();
                });
    }

    /** Valida que el número tenga el formato correcto según el tipo (RUC con dígito verificador, DNI, etc.). */
    private Mono<Void> validateDocumentNumberFormat(Integer documentTypeId, String documentNumber) {
        String number = documentNumber == null ? "" : documentNumber;
        return parameterRepositoryPort.findCodeById(documentTypeId)
                .flatMap(code -> {
                    String error = switch (code) {
                        case "RUC" -> isValidRuc(number) ? null
                                : "El RUC debe tener 11 dígitos y un dígito verificador válido.";
                        case "DNI" -> number.matches("\\d{8}") ? null
                                : "El DNI debe tener exactamente 8 dígitos.";
                        case "CE" -> number.matches("[A-Z0-9]{9,12}") ? null
                                : "El Carnet de Extranjería debe tener entre 9 y 12 caracteres alfanuméricos.";
                        case "PASAPORTE" -> number.matches("[A-Z0-9]{6,15}") ? null
                                : "El pasaporte debe tener entre 6 y 15 caracteres alfanuméricos.";
                        default -> null;
                    };
                    return error == null ? Mono.<Void>empty()
                            : Mono.<Void>error(new BusinessRuleException("INVALID_DOCUMENT_NUMBER", error));
                });
    }

    /** RUC peruano válido: 11 dígitos, prefijo conocido y dígito verificador (módulo 11). */
    private static boolean isValidRuc(String ruc) {
        if (ruc == null || !ruc.matches("\\d{11}")) {
            return false;
        }
        if (!Set.of("10", "15", "16", "17", "20").contains(ruc.substring(0, 2))) {
            return false;
        }
        int[] weights = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += (ruc.charAt(i) - '0') * weights[i];
        }
        int remainder = 11 - (sum % 11);
        int checkDigit = remainder == 10 ? 0 : (remainder == 11 ? 1 : remainder);
        return checkDigit == (ruc.charAt(10) - '0');
    }

    /** Un cliente no puede tener dos documentos del MISMO tipo (ej. dos RUC o dos DNI). */
    private Mono<Void> validateSingleDocumentPerType(Integer customerId, Integer documentTypeId, Integer excludeDocumentId) {
        return documentRepositoryPort.findByCustomerId(customerId)
                .filter(existing -> existing.documentTypeId().equals(documentTypeId))
                .filter(existing -> excludeDocumentId == null || !existing.documentId().equals(excludeDocumentId))
                .hasElements()
                .flatMap(exists -> Boolean.TRUE.equals(exists)
                        ? Mono.<Void>error(new BusinessRuleException(
                        "DUPLICATE_DOCUMENT_TYPE", "El cliente ya tiene registrado un documento de ese tipo."))
                        : Mono.<Void>empty());
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

    /** Número de documento: sin espacios extremos y en mayúsculas (RUC/DNI son dígitos; CE/Pasaporte se uniforman). */
    private String normalizeDocumentNumber(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toUpperCase();
    }
}
