package com.fmcg.ecommerce.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public class ReturnDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "ReturnRequest")
    public static class Request {
        private Long orderId;
        private String reason;
        private List<ReturnItemRequest> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReturnItemRequest {
        private Long orderItemId;
        private Integer qty;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String orderNumber;
        private String reason;
        private String status;
        private BigDecimal refundAmount;
        private LocalDateTime createdAt;
        private List<ReturnItemResponse> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReturnItemResponse {
        private Long id;
        private Long orderItemId;
        private String productTitle;
        private Integer qty;
    }
}
