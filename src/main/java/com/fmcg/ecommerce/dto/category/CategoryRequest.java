package com.fmcg.ecommerce.dto.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CategoryRequest {
    @NotBlank(message = "Category name is required")
    private String name;
    private String slug;
    private String description;
    private Long parentId;
    private String bannerUrl;
    private BigDecimal gstRate = BigDecimal.valueOf(5.0);
    private Integer sortOrder = 0;
    private Boolean isActive = true;
}
