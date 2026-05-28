package com.fmcg.ecommerce.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private Long parentId;
    private String bannerUrl;
    private BigDecimal gstRate;
    private Integer sortOrder;
    private boolean isActive;
    private int productCount;
    private List<CategoryResponse> children;
}
