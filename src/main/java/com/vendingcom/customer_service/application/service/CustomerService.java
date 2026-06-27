package com.vendingcom.customer_service.application.service;

import com.vendingcom.customer_service.application.dto.request.CreateCustomerRequest;
import com.vendingcom.customer_service.application.dto.request.UpdateCustomerRequest;
import com.vendingcom.customer_service.application.dto.response.PagedResponse;
import com.vendingcom.customer_service.application.port.input.CustomerUseCase;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerAuditLogRepositoryPort;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerParameterRepositoryPort;
import com.vendingcom.customer_service.application.port.output.persistence.CustomerRepositoryPort;
import com.vendingcom.customer_service.domain.exception.BusinessRuleException;
import com.vendingcom.customer_service.domain.exception.ResourceNotFoundException;
import com.vendingcom.customer_service.domain.model.Customer;
import com.vendingcom.customer_service.domain.model.CustomerAuditLog;
import com.vendingcom.customer_service.util.audit.AuditDataSerializer;
import com.vendingcom.customer_service.util.request.RequestContext;
import com.vendingcom.customer_service.util.request.RequestContextFilter;
import com.vendingcom.customer_service.util.security.JwtAuthenticationFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CustomerService implements CustomerUseCase {

    private static final String GROUP_CUSTOMER_TYPE = "CUSTOMER_TYPE";
    private static final String GROUP_CUSTOMER_STATUS = "CUSTOMER_STATUS";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_INACTIVE = "INACTIVE";
    private static final String TABLE_CUSTOMERS = "customers";

    private final CustomerRepositoryPort customerRepositoryPort;
    private final CustomerParameterRepositoryPort customerParameterRepositoryPort;
    private final CustomerAuditLogRepositoryPort customerAuditLogRepositoryPort;

    public CustomerService(
            CustomerRepositoryPort customerRepositoryPort,
            CustomerParameterRepositoryPort customerParameterRepositoryPort,
            CustomerAuditLogRepositoryPort customerAuditLogRepositoryPort
    ) {
        this.customerRepositoryPort = customerRepositoryPort;
        this.customerParameterRepositoryPort = customerParameterRepositoryPort;
        this.customerAuditLogRepositoryPort = customerAuditLogRepositoryPort;
    }

    @Override
    @Transactional
    public Mono<Customer> create(CreateCustomerRequest request) {
        return validateCustomerType(request.customerTypeId())
                .then(resolveStatusId(STATUS_ACTIVE))
                .flatMap(statusId -> currentActorId()
                        .flatMap(actor -> {
                            Customer customerToSave = new Customer(
                                    null,
                                    normalize(request.businessName()),
                                    normalize(request.tradeName()),
                                    request.customerTypeId(),
                                    normalizeEmail(request.mainEmail()),
                                    normalize(request.mainPhone()),
                                    normalize(request.website()),
                                    statusId,
                                    actor.orElse(null),
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                            );

                            return customerRepositoryPort.save(customerToSave)
                                    .flatMap(saved -> saveAudit(
                                            "CUSTOMER_CREATED",
                                            saved.customerId(),
                                            actor.orElse(null),
                                            "Cliente creado: " + saved.businessName(),
                                            null,
                                            AuditDataSerializer.serializeCustomer(saved)
                                    ).then(customerRepositoryPort.findById(saved.customerId())));
                        }));
    }

    @Override
    @Transactional
    public Mono<Customer> update(Integer customerId, UpdateCustomerRequest request) {
        return customerRepositoryPort.findById(customerId)
                .switchIfEmpty(notFound(customerId))
                .flatMap(existing -> validateCustomerType(request.customerTypeId())
                        .then(currentActorId())
                        .flatMap(actor -> {
                            Customer customerToUpdate = new Customer(
                                    existing.customerId(),
                                    normalize(request.businessName()),
                                    normalize(request.tradeName()),
                                    request.customerTypeId(),
                                    normalizeEmail(request.mainEmail()),
                                    normalize(request.mainPhone()),
                                    normalize(request.website()),
                                    existing.customerStatusId(),
                                    existing.createdByUserId(),
                                    actor.orElse(null),
                                    existing.createdAt(),
                                    LocalDateTime.now(),
                                    null,
                                    null
                            );

                            return customerRepositoryPort.save(customerToUpdate)
                                    .flatMap(updated -> saveAudit(
                                            "CUSTOMER_UPDATED",
                                            updated.customerId(),
                                            actor.orElse(null),
                                            "Cliente actualizado: " + updated.businessName(),
                                            AuditDataSerializer.serializeCustomer(existing),
                                            AuditDataSerializer.serializeCustomer(updated)
                                    ).then(customerRepositoryPort.findById(updated.customerId())));
                        }));
    }

    @Override
    public Mono<Customer> findById(Integer customerId) {
        return customerRepositoryPort.findById(customerId)
                .switchIfEmpty(notFound(customerId));
    }

    @Override
    public Mono<PagedResponse<Customer>> search(String search, Integer typeId, Integer statusId, int page, int size) {
        int safeSize = size <= 0 ? 20 : Math.min(size, 100);
        int safePage = Math.max(page, 0);
        long offset = (long) safePage * safeSize;
        String term = (search == null || search.isBlank()) ? null : search.trim();

        return customerRepositoryPort.search(term, typeId, statusId, safeSize, offset)
                .collectList()
                .zipWith(customerRepositoryPort.countSearch(term, typeId, statusId))
                .map(tuple -> PagedResponse.of(tuple.getT1(), safePage, safeSize, tuple.getT2()));
    }

    @Override
    @Transactional
    public Mono<Customer> activate(Integer customerId) {
        return changeStatus(customerId, STATUS_ACTIVE, "CUSTOMER_ACTIVATED", "Cliente activado: ");
    }

    @Override
    @Transactional
    public Mono<Void> deactivate(Integer customerId) {
        return changeStatus(customerId, STATUS_INACTIVE, "CUSTOMER_DEACTIVATED", "Cliente desactivado: ").then();
    }

    private Mono<Customer> changeStatus(Integer customerId, String statusCode, String action, String description) {
        return customerRepositoryPort.findById(customerId)
                .switchIfEmpty(notFound(customerId))
                .flatMap(existing -> resolveStatusId(statusCode)
                        .flatMap(statusId -> {
                            if (statusId.equals(existing.customerStatusId())) {
                                return Mono.error(new BusinessRuleException(
                                        "CUSTOMER_STATUS_UNCHANGED",
                                        "El cliente ya se encuentra en ese estado."
                                ));
                            }
                            return currentActorId().flatMap(actor -> {
                                Customer customerToUpdate = new Customer(
                                        existing.customerId(),
                                        existing.businessName(),
                                        existing.tradeName(),
                                        existing.customerTypeId(),
                                        existing.mainEmail(),
                                        existing.mainPhone(),
                                        existing.website(),
                                        statusId,
                                        existing.createdByUserId(),
                                        actor.orElse(null),
                                        existing.createdAt(),
                                        LocalDateTime.now(),
                                        null,
                                        null
                                );

                                return customerRepositoryPort.save(customerToUpdate)
                                        .flatMap(updated -> saveAudit(
                                                action,
                                                updated.customerId(),
                                                actor.orElse(null),
                                                description + updated.businessName(),
                                                AuditDataSerializer.serializeCustomer(existing),
                                                AuditDataSerializer.serializeCustomer(updated)
                                        ).then(customerRepositoryPort.findById(updated.customerId())));
                            });
                        }));
    }

    private Mono<Void> validateCustomerType(Integer customerTypeId) {
        return customerParameterRepositoryPort.existsByIdAndGroup(customerTypeId, GROUP_CUSTOMER_TYPE)
                .flatMap(exists -> Boolean.TRUE.equals(exists)
                        ? Mono.empty()
                        : Mono.error(new BusinessRuleException(
                        "INVALID_CUSTOMER_TYPE",
                        "El tipo de cliente indicado no es válido.")));
    }

    private Mono<Integer> resolveStatusId(String statusCode) {
        return customerParameterRepositoryPort.findIdByGroupAndCode(GROUP_CUSTOMER_STATUS, statusCode)
                .switchIfEmpty(Mono.error(new BusinessRuleException(
                        "CUSTOMER_STATUS_NOT_CONFIGURED",
                        "No está configurado el estado de cliente: " + statusCode)));
    }

    private <T> Mono<T> notFound(Integer customerId) {
        return Mono.error(new ResourceNotFoundException("No se encontró el cliente con id: " + customerId));
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
                    TABLE_CUSTOMERS,
                    customerId,
                    actionType,
                    description,
                    oldData,
                    newData,
                    clientIp,
                    userAgent,
                    resolvedExecutedBy,
                    LocalDateTime.now()
            );

            return customerAuditLogRepositoryPort.save(auditLog);
        });
    }

    private String normalize(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private String normalizeEmail(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim().toLowerCase();
    }
}
