package com.fmcg.ecommerce.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageDto {
    private Long id;
    private String publicId;
    private String url;
    private String alt;
    private boolean isPrimary;
    private Integer sortOrder;
}
