package com.fmcg.ecommerce.dto.order;

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
public class OrderResponse {
    private Long id;
    private String publicId;
    private String orderNumber;
    private String status;
    private String paymentMethod;
    private String paymentStatus;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal deliveryFee;
    private BigDecimal taxAmount;
    private BigDecimal total;
    private List<OrderItemResponse> items;
    private List<OrderStatusHistoryResponse> timeline;
    private AddressSnapshot deliveryAddress;
    private String customerName;
    private String customerEmail;
    private String deliveryPartnerName;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long productId;
        private String productTitle;
        private String sku;
        private String imageUrl;
        private Integer qty;
        private BigDecimal unitPrice;
        private BigDecimal discount;
        private BigDecimal total;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderStatusHistoryResponse {
        private String status;
        private String changedBy;
        private String notes;
        private LocalDateTime changedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressSnapshot {
        private String label;
        private String line1;
        private String line2;
        private String city;
        private String state;
        private String pincode;
    }
}
