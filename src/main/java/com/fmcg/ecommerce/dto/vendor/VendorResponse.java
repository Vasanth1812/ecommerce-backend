package com.fmcg.ecommerce.dto.vendor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorResponse {
    private Long id;
    private String businessName;
    private String contactName;
    private String email;
    private String phone;
    private String gstNumber;
    private String status;
    private BigDecimal commissionRate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
