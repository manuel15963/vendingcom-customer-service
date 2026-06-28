package com.vendingcom.customer_service.application.service;

import com.vendingcom.customer_service.application.dto.request.CreateAddressRequest;
import com.vendingcom.customer_service.application.dto.request.UpdateAddressRequest;
import com.vendingcom.customer_service.application.port.input.CustomerAddressUseCase;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerAddressRepositoryPort;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerAuditLogRepositoryPort;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerParameterRepositoryPort;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerRepositoryPort;
import com.vendingcom.customer_service.domain.exception.BusinessRuleException;
import com.vendingcom.customer_service.domain.exception.ResourceNotFoundException;
import com.vendingcom.customer_service.domain.model.CustomerAddress;
import com.vendingcom.customer_service.domain.model.CustomerAuditLog;
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
public class CustomerAddressService implements CustomerAddressUseCase {

    private static final String GROUP_ADDRESS_TYPE = "ADDRESS_TYPE";
    private static final String GROUP_ADDRESS_STATUS = "ADDRESS_STATUS";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_INACTIVE = "INACTIVE";
    private static final String DEFAULT_COUNTRY = "Perú";
    private static final String TABLE_ADDRESSES = "customer_addresses";

    private final CustomerAddressRepositoryPort addressRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final CustomerParameterRepositoryPort parameterRepositoryPort;
    private final CustomerAuditLogRepositoryPort auditLogRepositoryPort;

    public CustomerAddressService(
            CustomerAddressRepositoryPort addressRepositoryPort,
            CustomerRepositoryPort customerRepositoryPort,
            CustomerParameterRepositoryPort parameterRepositoryPort,
            CustomerAuditLogRepositoryPort auditLogRepositoryPort
    ) {
        this.addressRepositoryPort = addressRepositoryPort;
        this.customerRepositoryPort = customerRepositoryPort;
        this.parameterRepositoryPort = parameterRepositoryPort;
        this.auditLogRepositoryPort = auditLogRepositoryPort;
    }

    @Override
    @Transactional
    public Mono<CustomerAddress> create(Integer customerId, CreateAddressRequest request) {
        return ensureCustomerActive(customerId)
                .then(validateAddressType(request.addressTypeId()))
                .then(resolveStatusId(STATUS_ACTIVE))
                .flatMap(statusId -> currentActorId().flatMap(actor -> {
                    boolean primary = Boolean.TRUE.equals(request.isPrimary());
                    return clearPrimaryIfNeeded(customerId, primary)
                            .then(Mono.defer(() -> {
                                CustomerAddress toSave = new CustomerAddress(
                                        null,
                                        customerId,
                                        request.addressTypeId(),
                                        normalize(request.addressLine()),
                                        normalize(request.district()),
                                        normalize(request.province()),
                                        normalize(request.department()),
                                        countryOrDefault(request.country()),
                                        normalize(request.reference()),
                                        primary,
                                        statusId,
                                        actor.orElse(null),
                                        null,
                                        null,
                                        null,
                                        null,
                                        null
                                );
                                return addressRepositoryPort.save(toSave)
                                        .flatMap(saved -> saveAudit(
                                                "ADDRESS_CREATED", customerId, saved.addressId(), actor.orElse(null),
                                                "Dirección creada para el cliente " + customerId,
                                                null, AuditDataSerializer.serialize(saved)
                                        ).then(addressRepositoryPort.findById(saved.addressId())));
                            }));
                }));
    }

    @Override
    public Flux<CustomerAddress> findByCustomer(Integer customerId) {
        return ensureCustomerExists(customerId)
                .thenMany(addressRepositoryPort.findByCustomerId(customerId));
    }

    @Override
    public Mono<CustomerAddress> findById(Integer customerId, Integer addressId) {
        return findAddressOfCustomer(customerId, addressId);
    }

    @Override
    @Transactional
    public Mono<CustomerAddress> update(Integer customerId, Integer addressId, UpdateAddressRequest request) {
        return findAddressOfCustomer(customerId, addressId)
                .flatMap(existing -> validateAddressType(request.addressTypeId())
                        .then(currentActorId())
                        .flatMap(actor -> {
                            boolean primary = Boolean.TRUE.equals(request.isPrimary());
                            boolean needsClear = primary && !Boolean.TRUE.equals(existing.isPrimary());
                            return clearPrimaryIfNeeded(customerId, needsClear)
                                    .then(Mono.defer(() -> {
                                        CustomerAddress toUpdate = new CustomerAddress(
                                                existing.addressId(),
                                                existing.customerId(),
                                                request.addressTypeId(),
                                                normalize(request.addressLine()),
                                                normalize(request.district()),
                                                normalize(request.province()),
                                                normalize(request.department()),
                                                countryOrDefault(request.country()),
                                                normalize(request.reference()),
                                                primary,
                                                existing.addressStatusId(),
                                                existing.createdByUserId(),
                                                actor.orElse(null),
                                                existing.createdAt(),
                                                LocalDateTime.now(),
                                                null,
                                                null
                                        );
                                        return addressRepositoryPort.save(toUpdate)
                                                .flatMap(updated -> saveAudit(
                                                        "ADDRESS_UPDATED", customerId, updated.addressId(), actor.orElse(null),
                                                        "Dirección actualizada del cliente " + customerId,
                                                        AuditDataSerializer.serialize(existing), AuditDataSerializer.serialize(updated)
                                                ).then(addressRepositoryPort.findById(updated.addressId())));
                                    }));
                        }));
    }

    @Override
    @Transactional
    public Mono<CustomerAddress> activate(Integer customerId, Integer addressId) {
        return changeStatus(customerId, addressId, STATUS_ACTIVE, "ADDRESS_ACTIVATED", "Dirección activada del cliente ");
    }

    @Override
    @Transactional
    public Mono<Void> deactivate(Integer customerId, Integer addressId) {
        return changeStatus(customerId, addressId, STATUS_INACTIVE, "ADDRESS_DEACTIVATED", "Dirección desactivada del cliente ").then();
    }

    private Mono<CustomerAddress> changeStatus(Integer customerId, Integer addressId, String statusCode, String action, String description) {
        return findAddressOfCustomer(customerId, addressId)
                .flatMap(existing -> resolveStatusId(statusCode).flatMap(statusId -> {
                    if (statusId.equals(existing.addressStatusId())) {
                        return Mono.error(new BusinessRuleException(
                                "ADDRESS_STATUS_UNCHANGED", "La dirección ya se encuentra en ese estado."));
                    }
                    return currentActorId().flatMap(actor -> {
                        CustomerAddress toUpdate = new CustomerAddress(
                                existing.addressId(),
                                existing.customerId(),
                                existing.addressTypeId(),
                                existing.addressLine(),
                                existing.district(),
                                existing.province(),
                                existing.department(),
                                existing.country(),
                                existing.reference(),
                                STATUS_INACTIVE.equals(statusCode) ? Boolean.FALSE : existing.isPrimary(),
                                statusId,
                                existing.createdByUserId(),
                                actor.orElse(null),
                                existing.createdAt(),
                                LocalDateTime.now(),
                                null,
                                null
                        );
                        return addressRepositoryPort.save(toUpdate)
                                .flatMap(updated -> saveAudit(
                                        action, customerId, updated.addressId(), actor.orElse(null),
                                        description + customerId,
                                        AuditDataSerializer.serialize(existing), AuditDataSerializer.serialize(updated)
                                ).then(addressRepositoryPort.findById(updated.addressId())));
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

    private Mono<CustomerAddress> findAddressOfCustomer(Integer customerId, Integer addressId) {
        return addressRepositoryPort.findById(addressId)
                .filter(address -> address.customerId().equals(customerId))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "No se encontró la dirección " + addressId + " para el cliente " + customerId)));
    }

    private Mono<Void> validateAddressType(Integer addressTypeId) {
        return parameterRepositoryPort.existsByIdAndGroup(addressTypeId, GROUP_ADDRESS_TYPE)
                .flatMap(exists -> Boolean.TRUE.equals(exists)
                        ? Mono.empty()
                        : Mono.error(new BusinessRuleException(
                        "INVALID_ADDRESS_TYPE", "El tipo de dirección indicado no es válido.")));
    }

    private Mono<Void> clearPrimaryIfNeeded(Integer customerId, boolean primary) {
        return primary ? addressRepositoryPort.clearPrimaryFlag(customerId) : Mono.empty();
    }

    private Mono<Integer> resolveStatusId(String statusCode) {
        return parameterRepositoryPort.findIdByGroupAndCode(GROUP_ADDRESS_STATUS, statusCode)
                .switchIfEmpty(Mono.error(new BusinessRuleException(
                        "ADDRESS_STATUS_NOT_CONFIGURED", "No está configurado el estado de dirección: " + statusCode)));
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
                    TABLE_ADDRESSES,
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

    private String countryOrDefault(String value) {
        String normalized = normalize(value);
        return normalized == null ? DEFAULT_COUNTRY : normalized;
    }
}
