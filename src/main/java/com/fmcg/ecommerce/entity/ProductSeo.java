package com.fmcg.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "product_seo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSeo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"seo", "hibernateLazyInitializer", "handler", "images", "category"})
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Product product;

    private String metaTitle;

    @Column(length = 500)
    private String metaDescription;

    @ElementCollection
    @CollectionTable(name = "product_seo_keywords", joinColumns = @JoinColumn(name = "product_seo_id"))
    @Column(name = "keyword")
    private List<String> metaKeywords;

    @Column(unique = true)
    private String slug;

    private String canonicalUrl;

    private String ogImage;
}
