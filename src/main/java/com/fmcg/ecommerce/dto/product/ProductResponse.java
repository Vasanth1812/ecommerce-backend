package com.fmcg.ecommerce.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String sku;
    private String barcode;
    private String title;
    private String description;
    private String shortDescription;
    private String brand;
    private Long categoryId;
    private String categoryName;
    private BigDecimal price;
    private BigDecimal mrp;
    private BigDecimal costPrice;
    private BigDecimal taxRate;
    private String unit;
    private String weight;
    private String status;
    private String tags;
    private String warehouse;
    private String supplier;
    private List<ProductImageDto> images;
    private StockInfo stock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockInfo {
        private Integer qtyAvailable;
        private Integer qtyReserved;
        private String stockStatus; // IN_STOCK / LOW_STOCK / OUT_OF_STOCK
    }
}
