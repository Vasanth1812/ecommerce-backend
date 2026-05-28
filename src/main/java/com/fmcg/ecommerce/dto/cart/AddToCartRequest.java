package com.fmcg.ecommerce.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddToCartRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;
    private String sku;
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer qty;
}
