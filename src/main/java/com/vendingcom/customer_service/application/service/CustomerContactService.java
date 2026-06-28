package com.vendingcom.customer_service.application.service;

import com.vendingcom.customer_service.application.dto.request.CreateContactRequest;
import com.vendingcom.customer_service.application.dto.request.UpdateContactRequest;
import com.vendingcom.customer_service.application.port.input.CustomerContactUseCase;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerAuditLogRepositoryPort;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerContactRepositoryPort;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerParameterRepositoryPort;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerRepositoryPort;
import com.vendingcom.customer_service.domain.exception.BusinessRuleException;
import com.vendingcom.customer_service.domain.exception.ResourceNotFoundException;
import com.vendingcom.customer_service.domain.model.CustomerAuditLog;
import com.vendingcom.customer_service.domain.model.CustomerContact;
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
public class CustomerContactService implements CustomerContactUseCase {

    private static final String GROUP_CONTACT_STATUS = "CONTACT_STATUS";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_INACTIVE = "INACTIVE";
    private static final String TABLE_CONTACTS = "customer_contacts";

    private final CustomerContactRepositoryPort contactRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final CustomerParameterRepositoryPort parameterRepositoryPort;
    private final CustomerAuditLogRepositoryPort auditLogRepositoryPort;

    public CustomerContactService(
            CustomerContactRepositoryPort contactRepositoryPort,
            CustomerRepositoryPort customerRepositoryPort,
            CustomerParameterRepositoryPort parameterRepositoryPort,
            CustomerAuditLogRepositoryPort auditLogRepositoryPort
    ) {
        this.contactRepositoryPort = contactRepositoryPort;
        this.customerRepositoryPort = customerRepositoryPort;
        this.parameterRepositoryPort = parameterRepositoryPort;
        this.auditLogRepositoryPort = auditLogRepositoryPort;
    }

    @Override
    @Transactional
    public Mono<CustomerContact> create(Integer customerId, CreateContactRequest request) {
        return ensureCustomerActive(customerId)
                .then(resolveStatusId(STATUS_ACTIVE))
                .flatMap(statusId -> currentActorId().flatMap(actor -> {
                    boolean primary = Boolean.TRUE.equals(request.isPrimary());
                    return clearPrimaryIfNeeded(customerId, primary)
                            .then(Mono.defer(() -> {
                                CustomerContact toSave = new CustomerContact(
                                        null,
                                        customerId,
                                        normalize(request.fullName()),
                                        normalize(request.position()),
                                        normalizeEmail(request.email()),
                                        normalize(request.phone()),
                                        primary,
                                        statusId,
                                        actor.orElse(null),
                                        null,
                                        null,
                                        null,
                                        null
                                );
                                return contactRepositoryPort.save(toSave)
                                        .flatMap(saved -> saveAudit(
                                                "CONTACT_CREATED", customerId, saved.contactId(), actor.orElse(null),
                                                "Contacto creado: " + saved.fullName(),
                                                null, AuditDataSerializer.serialize(saved)
                                        ).then(contactRepositoryPort.findById(saved.contactId())));
                            }));
                }));
    }

    @Override
    public Flux<CustomerContact> findByCustomer(Integer customerId) {
        return ensureCustomerExists(customerId)
                .thenMany(contactRepositoryPort.findByCustomerId(customerId));
    }

    @Override
    public Mono<CustomerContact> findById(Integer customerId, Integer contactId) {
        return findContactOfCustomer(customerId, contactId);
    }

    @Override
    @Transactional
    public Mono<CustomerContact> update(Integer customerId, Integer contactId, UpdateContactRequest request) {
        return findContactOfCustomer(customerId, contactId)
                .flatMap(existing -> currentActorId().flatMap(actor -> {
                    boolean primary = Boolean.TRUE.equals(request.isPrimary());
                    boolean needsClear = primary && !Boolean.TRUE.equals(existing.isPrimary());
                    return clearPrimaryIfNeeded(customerId, needsClear)
                            .then(Mono.defer(() -> {
                                CustomerContact toUpdate = new CustomerContact(
                                        existing.contactId(),
                                        existing.customerId(),
                                        normalize(request.fullName()),
                                        normalize(request.position()),
                                        normalizeEmail(request.email()),
                                        normalize(request.phone()),
                                        primary,
                                        existing.contactStatusId(),
                                        existing.createdByUserId(),
                                        actor.orElse(null),
                                        existing.createdAt(),
                                        LocalDateTime.now(),
                                        null
                                );
                                return contactRepositoryPort.save(toUpdate)
                                        .flatMap(updated -> saveAudit(
                                                "CONTACT_UPDATED", customerId, updated.contactId(), actor.orElse(null),
                                                "Contacto actualizado: " + updated.fullName(),
                                                AuditDataSerializer.serialize(existing), AuditDataSerializer.serialize(updated)
                                        ).then(contactRepositoryPort.findById(updated.contactId())));
                            }));
                }));
    }

    @Override
    @Transactional
    public Mono<CustomerContact> activate(Integer customerId, Integer contactId) {
        return changeStatus(customerId, contactId, STATUS_ACTIVE, "CONTACT_ACTIVATED", "Contacto activado: ");
    }

    @Override
    @Transactional
    public Mono<Void> deactivate(Integer customerId, Integer contactId) {
        return changeStatus(customerId, contactId, STATUS_INACTIVE, "CONTACT_DEACTIVATED", "Contacto desactivado: ").then();
    }

    private Mono<CustomerContact> changeStatus(Integer customerId, Integer contactId, String statusCode, String action, String description) {
        return findContactOfCustomer(customerId, contactId)
                .flatMap(existing -> resolveStatusId(statusCode).flatMap(statusId -> {
                    if (statusId.equals(existing.contactStatusId())) {
                        return Mono.error(new BusinessRuleException(
                                "CONTACT_STATUS_UNCHANGED", "El contacto ya se encuentra en ese estado."));
                    }
                    return currentActorId().flatMap(actor -> {
                        CustomerContact toUpdate = new CustomerContact(
                                existing.contactId(),
                                existing.customerId(),
                                existing.fullName(),
                                existing.position(),
                                existing.email(),
                                existing.phone(),
                                STATUS_INACTIVE.equals(statusCode) ? Boolean.FALSE : existing.isPrimary(),
                                statusId,
                                existing.createdByUserId(),
                                actor.orElse(null),
                                existing.createdAt(),
                                LocalDateTime.now(),
                                null
                        );
                        return contactRepositoryPort.save(toUpdate)
                                .flatMap(updated -> saveAudit(
                                        action, customerId, updated.contactId(), actor.orElse(null),
                                        description + updated.fullName(),
                                        AuditDataSerializer.serialize(existing), AuditDataSerializer.serialize(updated)
                                ).then(contactRepositoryPort.findById(updated.contactId())));
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

    private Mono<CustomerContact> findContactOfCustomer(Integer customerId, Integer contactId) {
        return contactRepositoryPort.findById(contactId)
                .filter(contact -> contact.customerId().equals(customerId))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "No se encontró el contacto " + contactId + " para el cliente " + customerId)));
    }

    private Mono<Void> clearPrimaryIfNeeded(Integer customerId, boolean primary) {
        return primary ? contactRepositoryPort.clearPrimaryFlag(customerId) : Mono.empty();
    }

    private Mono<Integer> resolveStatusId(String statusCode) {
        return parameterRepositoryPort.findIdByGroupAndCode(GROUP_CONTACT_STATUS, statusCode)
                .switchIfEmpty(Mono.error(new BusinessRuleException(
                        "CONTACT_STATUS_NOT_CONFIGURED", "No está configurado el estado de contacto: " + statusCode)));
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
                    TABLE_CONTACTS,
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

    private String normalizeEmail(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim().toLowerCase();
    }
}
