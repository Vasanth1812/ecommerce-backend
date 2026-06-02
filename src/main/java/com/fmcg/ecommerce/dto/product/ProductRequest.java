package com.fmcg.ecommerce.dto.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductRequest {
    @NotBlank(message = "Product title is required")
    private String title;
    @NotBlank(message = "SKU is required")
    private String sku;
    private String barcode;
    @NotNull(message = "Category is required")
    private Long categoryId;
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    private BigDecimal mrp;
    private BigDecimal costPrice;
    private String brand;
    private String unit;
    private String weight;
    private BigDecimal taxRate = BigDecimal.valueOf(5.0);
    private String status = "ACTIVE";
    private String description;
    private String shortDescription;
    private List<String> tags;
    private String warehouse;
    private String supplier;
}
