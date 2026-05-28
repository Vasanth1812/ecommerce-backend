package com.fmcg.ecommerce.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCartItemRequest {
    @NotNull
    @Min(value = 0, message = "Quantity must be 0 or more (0 to remove)")
    private Integer qty;
}
