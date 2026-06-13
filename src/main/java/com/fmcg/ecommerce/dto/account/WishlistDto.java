package com.fmcg.ecommerce.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

public class WishlistDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "WishlistRequest")
    public static class Request {
        private Long productId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long productId;
        private String productTitle;
        private String productSku;
        private String imageUrl;
        private BigDecimal price;
        private BigDecimal effectivePrice;
        private String stockStatus;
    }
}
