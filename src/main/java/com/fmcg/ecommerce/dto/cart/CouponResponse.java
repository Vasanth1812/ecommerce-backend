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
public class CouponResponse {
    private String code;
    private String type; // PERCENTAGE / FIXED / FREE_DELIVERY
    private BigDecimal discountValue;
    private String discountType; // PERCENTAGE / FIXED
    private BigDecimal minOrder;
    private String description; // e.g. "minimum order 299 for this coupon SAVE200"
}
