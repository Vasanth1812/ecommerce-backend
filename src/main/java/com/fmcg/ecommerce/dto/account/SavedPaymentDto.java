package com.fmcg.ecommerce.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class SavedPaymentDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String type; // CARD or UPI
        private String provider; // VISA, MASTERCARD, GPAY, etc.
        private String last4;
        private String upiId;
        private String token;
        private Boolean isDefault;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String type;
        private String provider;
        private String last4;
        private String upiId;
        private Boolean isDefault;
        // Notice we DO NOT return the secure token back to the frontend for security reasons
    }
}
