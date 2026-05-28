package com.fmcg.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties({"items", "statusHistory", "user", "deliveryAddress", "appliedCoupon"})
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonIgnoreProperties({"images", "category"})
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Product product;

    private String productTitle; // snapshot at time of order

    private String sku;

    @Column(nullable = false)
    private Integer qty;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal discount;

    private Long substitutedProductId;
}
