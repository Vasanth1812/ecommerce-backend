package com.fmcg.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties({"items", "statusHistory", "user", "deliveryAddress", "appliedCoupon"})
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Order order;

    @Column(nullable = false)
    private String status;

    private String changedBy;

    private String notes;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime changedAt;
}
