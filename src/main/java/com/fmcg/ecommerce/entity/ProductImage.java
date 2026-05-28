package com.fmcg.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"images", "category"})
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Product product;

    @Column(nullable = false)
    private String url;

    private String alt;

    @Builder.Default
    private Boolean isPrimary = false;

    @Builder.Default
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime uploadedAt;
}
