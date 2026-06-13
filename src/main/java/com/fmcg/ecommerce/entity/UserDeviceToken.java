package com.fmcg.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_device_tokens", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "device_token"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @Column(nullable = false)
    private String deviceToken;

    @Builder.Default
    private String deviceType = "UNKNOWN"; // iOS, ANDROID, WEB

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
