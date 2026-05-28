package com.fmcg.ecommerce.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRequest {
    @NotNull(message = "Delivery address is required")
    private Long addressId;
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
    private String couponCode;
    private Integer loyaltyPointsBurn = 0;
    private String notes;
}
