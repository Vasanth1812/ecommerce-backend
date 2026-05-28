package com.fmcg.ecommerce.dto.product;

import lombok.Data;

import java.util.List;

@Data
public class UpdateProductSeoRequest {
    private String metaTitle;
    private String metaDescription;
    private List<String> metaKeywords;
    private String slug;
    private String canonicalUrl;
    private String ogImage;
}
