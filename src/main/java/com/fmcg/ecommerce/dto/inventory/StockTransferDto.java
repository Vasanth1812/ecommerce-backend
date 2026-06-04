package com.fmcg.ecommerce.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransferDto {
    private Long id;
    private String transferNumber;
    private Long productId;
    private String productName;
    private Long fromWarehouseId;
    private String fromWarehouseName;
    private Long toWarehouseId;
    private String toWarehouseName;
    private Integer quantity;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
