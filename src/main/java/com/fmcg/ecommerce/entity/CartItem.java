package com.fmcg.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonIgnoreProperties({"items", "user", "appliedCoupon"})
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"images", "category"})
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Product product;

    private String sku;

    @Column(nullable = false)
    private Integer qty;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime addedAt;
}
