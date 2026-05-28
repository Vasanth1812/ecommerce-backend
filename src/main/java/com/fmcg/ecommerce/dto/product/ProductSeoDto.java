package com.fmcg.ecommerce.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSeoDto {
    private Long productId;
    private String productName;
    private String sku;
    private String metaTitle;
    private String metaDescription;
    private List<String> metaKeywords;
    private String slug;
    private String canonicalUrl;
    private String ogImage;
}
