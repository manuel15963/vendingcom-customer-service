package com.vendingcom.customer_service.infrastructure.adapters.outbound.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customer_addresses")
public class CustomerAddressEntity {

    @Id
    @Column("address_id")
    private Integer addressId;

    @Column("customer_id")
    private Integer customerId;

    @Column("address_type_id")
    private Integer addressTypeId;

    @Column("address_line")
    private String addressLine;

    @Column("district")
    private String district;

    @Column("province")
    private String province;

    @Column("department")
    private String department;

    @Column("country")
    private String country;

    @Column("reference")
    private String reference;

    @Column("is_primary")
    private Boolean isPrimary;

    @Column("address_status_id")
    private Integer addressStatusId;

    @Column("created_by_user_id")
    private Integer createdByUserId;

    @Column("updated_by_user_id")
    private Integer updatedByUserId;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
