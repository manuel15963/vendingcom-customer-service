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
@Table(name = "customer_contacts")
public class CustomerContactEntity {

    @Id
    @Column("contact_id")
    private Integer contactId;

    @Column("customer_id")
    private Integer customerId;

    @Column("full_name")
    private String fullName;

    @Column("position")
    private String position;

    @Column("email")
    private String email;

    @Column("phone")
    private String phone;

    @Column("is_primary")
    private Boolean isPrimary;

    @Column("contact_status_id")
    private Integer contactStatusId;

    @Column("created_by_user_id")
    private Integer createdByUserId;

    @Column("updated_by_user_id")
    private Integer updatedByUserId;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
