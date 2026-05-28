package com.fmcg.ecommerce.dto.cart;

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
public class CartResponse {
    private Long id;
    private List<CartItemResponse> items;
    private int itemCount;
    private BigDecimal subtotal;
    private BigDecimal couponDiscount;
    private BigDecimal deliveryFee;
    private BigDecimal tax;
    private BigDecimal total;
    private String couponCode;
    private boolean freeDelivery;
    private BigDecimal minForFreeDelivery;
}
