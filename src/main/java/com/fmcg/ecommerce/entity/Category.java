package com.fmcg.ecommerce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String slug;

    private String description;

    private Long parentId;

    private String bannerUrl;

    @Builder.Default
    @Column(precision = 5, scale = 2)
    private BigDecimal gstRate = BigDecimal.valueOf(5.0);

    @Builder.Default
    private Integer sortOrder = 0;

    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Transient
    private int productCount;
}
