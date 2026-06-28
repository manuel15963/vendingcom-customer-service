package com.vendingcom.customer_service.application.port.output.persistence;

import com.vendingcom.customer_service.domain.model.CustomerParameter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Acceso al catálogo de parámetros (tipos y estados) del módulo.
 */
public interface CustomerParameterRepositoryPort {

    /** Resuelve el id de un parámetro activo por su grupo y código (ej: CUSTOMER_STATUS / ACTIVE). */
    Mono<Integer> findIdByGroupAndCode(String parameterGroup, String parameterCode);

    /** Verifica que un parámetro exista y pertenezca al grupo indicado (ej: que el id sea un CUSTOMER_TYPE). */
    Mono<Boolean> existsByIdAndGroup(Integer parameterId, String parameterGroup);

    /** Devuelve el código de un parámetro por su id (ej: 3 -> "EMPRESA"). Vacío si no existe. */
    Mono<String> findCodeById(Integer parameterId);

    /** Lista los parámetros activos de un grupo, ordenados por sort_order. */
    Flux<CustomerParameter> findActiveByGroup(String parameterGroup);

    /** Lista todos los parámetros activos del módulo. */
    Flux<CustomerParameter> findAllActive();
}
