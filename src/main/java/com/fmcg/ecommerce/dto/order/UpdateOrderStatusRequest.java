package com.fmcg.ecommerce.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    @NotBlank(message = "Status is required")
    private String status;
    private String notes;
}
