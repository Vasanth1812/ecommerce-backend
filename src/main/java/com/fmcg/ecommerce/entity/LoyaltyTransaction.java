package com.fmcg.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "loyalty_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"addresses", "cart", "orders", "loyaltyAccount", "passwordHash"})
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    private Long orderId;

    @Column(nullable = false)
    private String action; // EARN / BURN / EXPIRE / BONUS

    @Builder.Default
    private Integer pointsEarned = 0;

    @Builder.Default
    private Integer pointsBurned = 0;

    private String description;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
