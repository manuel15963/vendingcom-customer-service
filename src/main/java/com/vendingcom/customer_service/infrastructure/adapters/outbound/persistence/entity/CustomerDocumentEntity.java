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
@Table(name = "customer_documents")
public class CustomerDocumentEntity {

    @Id
    @Column("document_id")
    private Integer documentId;

    @Column("customer_id")
    private Integer customerId;

    @Column("document_type_id")
    private Integer documentTypeId;

    @Column("document_number")
    private String documentNumber;

    @Column("file_url")
    private String fileUrl;

    @Column("is_primary")
    private Boolean isPrimary;

    @Column("document_status_id")
    private Integer documentStatusId;

    @Column("created_by_user_id")
    private Integer createdByUserId;

    @Column("updated_by_user_id")
    private Integer updatedByUserId;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
