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
@Table(name = "customers")
public class CustomerEntity {

    @Id
    @Column("customer_id")
    private Integer customerId;

    @Column("business_name")
    private String businessName;

    @Column("trade_name")
    private String tradeName;

    @Column("customer_type_id")
    private Integer customerTypeId;

    @Column("main_email")
    private String mainEmail;

    @Column("main_phone")
    private String mainPhone;

    @Column("website")
    private String website;

    @Column("customer_status_id")
    private Integer customerStatusId;

    @Column("created_by_user_id")
    private Integer createdByUserId;

    @Column("updated_by_user_id")
    private Integer updatedByUserId;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
