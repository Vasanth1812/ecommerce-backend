package com.fmcg.ecommerce.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long id;
    private Long productId;
    private String title;
    private String sku;
    private String imageUrl;
    private String brand;
    private String unit;
    private Integer qty;
    private BigDecimal unitPrice;
    private BigDecimal discountPrice;
    private String activePromotionName;
    private BigDecimal mrp;
    private BigDecimal total;
    private String stockStatus;
    private Integer maxQty;
    private Boolean isBogoActive;
}
