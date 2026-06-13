package com.fmcg.ecommerce.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public class ShoppingListDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "ShoppingListCreateRequest")
    public static class CreateListRequest {
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "ShoppingListItemAddRequest")
    public static class AddItemRequest {
        private Long productId;
        private Integer qty;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListResponse {
        private Long id;
        private String name;
        private LocalDateTime createdAt;
        private List<ItemResponse> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemResponse {
        private Long id;
        private Long productId;
        private String productTitle;
        private String imageUrl;
        private BigDecimal price;
        private Integer qty;
    }
}
