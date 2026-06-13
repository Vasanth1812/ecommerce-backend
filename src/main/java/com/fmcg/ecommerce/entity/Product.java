package com.fmcg.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

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

    @Column(unique = true)
    private String sku;

    @Column(unique = true)
    private String barcode;

    @Column(name = "item_code", unique = true, nullable = false)
    private String itemCode;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String shortDescription;

    private String brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Category category;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ProductSeo seo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal mrp;

    @Column(precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Builder.Default
    @Column(precision = 5, scale = 2)
    private BigDecimal taxRate = BigDecimal.valueOf(5.0);

    private String unit;

    private String weight;

    @Column(nullable = false)
    private String status; // ACTIVE / INACTIVE / DRAFT

    @Column(columnDefinition = "TEXT")
    private String tags;

    private String warehouse;

    private String supplier;

    @Column(columnDefinition = "boolean default false")
    private Boolean isBogoActive = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToMany(mappedBy = "eligibleProducts", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private java.util.List<Promotion> promotions;

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnoreProperties("product")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ProductImage> images = new ArrayList<>();

    // --- Helper Methods ---

    @Transient
    public BigDecimal getEffectivePrice() {
        if (promotions == null || promotions.isEmpty()) return price;
        
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        BigDecimal lowestPrice = price;

        for (Promotion promo : promotions) {
            if ("ACTIVE".equals(promo.getStatus()) &&
                    (promo.getStartDate() == null || !now.isBefore(promo.getStartDate())) &&
                    (promo.getEndDate() == null || !now.isAfter(promo.getEndDate()))) {

                BigDecimal candidatePrice = price;
                if ("PERCENTAGE".equals(promo.getDiscountType()) && promo.getDiscountValue() != null) {
                    BigDecimal discountAmount = price.multiply(promo.getDiscountValue().divide(BigDecimal.valueOf(100)));
                    candidatePrice = price.subtract(discountAmount);
                } else if ("FIXED".equals(promo.getDiscountType()) && promo.getDiscountValue() != null) {
                    candidatePrice = price.subtract(promo.getDiscountValue());
                }

                if (candidatePrice.compareTo(BigDecimal.ZERO) < 0) {
                    candidatePrice = BigDecimal.ZERO;
                }

                if (candidatePrice.compareTo(lowestPrice) < 0) {
                    lowestPrice = candidatePrice;
                }
            }
        }
        return lowestPrice;
    }
}
