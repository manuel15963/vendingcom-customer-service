package com.vendingcom.customer_service.infrastructure.adapters.outbound.persistence.repository;

import com.vendingcom.customer_service.infrastructure.adapters.outbound.persistence.entity.CustomerEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ReactiveCustomerRepository extends ReactiveCrudRepository<CustomerEntity, Integer> {
}
