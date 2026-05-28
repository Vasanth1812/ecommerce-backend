package com.fmcg.ecommerce.dto.vendor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VendorRequest {
    @NotBlank(message = "Business name is required")
    private String businessName;

    private String contactName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone is required")
    private String phone;

    private String gstNumber;

    private String status;

    private BigDecimal commissionRate;
}
