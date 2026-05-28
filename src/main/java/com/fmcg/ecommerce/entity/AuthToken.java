package com.fmcg.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "auth_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(unique = true, length = 512)
    private String refreshToken;

    private String deviceId;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Builder.Default
    private Boolean revoked = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
