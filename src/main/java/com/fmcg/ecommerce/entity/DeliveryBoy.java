package com.fmcg.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_boys")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryBoy {

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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    private String vehicleType;
        private String vehicleNumber;
    private String zone;

    @Builder.Default
    @Column(nullable = false)
    private String availabilityStatus = "FREE"; // FREE, ASSIGNED, OFFLINE

    private Double currentLat;
    private Double currentLng;

    private LocalDateTime lastLocationUpdate;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
