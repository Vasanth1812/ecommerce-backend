package com.fmcg.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpSession {

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
    private String identifier; // email or mobile

    @Column(nullable = false)
    private String otpHash;

    @Column(nullable = false)
    private String channel; // EMAIL / SMS

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Builder.Default
    private Integer attempts = 0;

    @Builder.Default
    private Boolean used = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
