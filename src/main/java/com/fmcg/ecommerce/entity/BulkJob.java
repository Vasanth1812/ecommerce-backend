package com.fmcg.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "bulk_jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", unique = true, updatable = false)
    private String publicId;

    @PrePersist
    protected void onCreatePublicId() {
        if (this.publicId == null) {
            this.publicId = java.util.UUID.randomUUID().toString();
        }
    }

    @Column(nullable = false)
    private String jobType; // e.g., "PRODUCT_IMPORT"

    @Column(nullable = false)
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED

    private Integer totalRows;
    private Integer processedRows;
    private Integer failedRows;

    @Column(columnDefinition = "TEXT")
    private String errorLog;

    @Column(nullable = false)
    private Long startedByUserId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}