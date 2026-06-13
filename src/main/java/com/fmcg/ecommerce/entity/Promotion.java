package com.fmcg.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {

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

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type; // DISCOUNT / COUPON / BUY_X_GET_Y / FLASH_SALE

    private String description;

    @Column(precision = 10, scale = 2)
    private BigDecimal discountValue;

    private String discountType; // PERCENTAGE / FIXED

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Column(nullable = false)
    private String status; // ACTIVE / SCHEDULED / EXPIRED

    @Builder.Default
    private Integer usageCount = 0;

    private Integer maxUses;

    @Column(precision = 10, scale = 2)
    private BigDecimal minOrder;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "promotion_products",
            joinColumns = @JoinColumn(name = "promotion_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private java.util.List<Product> eligibleProducts;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
