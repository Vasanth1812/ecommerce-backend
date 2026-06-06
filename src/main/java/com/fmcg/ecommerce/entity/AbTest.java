package com.fmcg.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ab_tests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbTest {

    @Column(name = "public_id", unique = true, updatable = false)
    private String publicId;

    @PrePersist
    protected void onCreatePublicId() {
        if (this.publicId == null) {
            this.publicId = java.util.UUID.randomUUID().toString();
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "variant_a_name", nullable = false)
    private String variantAName;

    @Column(name = "variant_b_name", nullable = false)
    private String variantBName;

    private String winner; // VARIANT_A, VARIANT_B, INCONCLUSIVE

    @Builder.Default
    @Column(nullable = false)
    private String status = "RUNNING"; // RUNNING, COMPLETED

    @Builder.Default
    @Column(name = "impressions_a")
    private Integer impressionsA = 0;

    @Builder.Default
    @Column(name = "impressions_b")
    private Integer impressionsB = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
