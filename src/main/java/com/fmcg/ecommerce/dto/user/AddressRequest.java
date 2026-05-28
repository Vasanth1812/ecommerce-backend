package com.fmcg.ecommerce.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddressRequest {
    @NotBlank(message = "Label is required")
    private String label;
    @NotBlank(message = "Address line 1 is required")
    private String line1;
    private String line2;
    @NotBlank(message = "City is required")
    private String city;
    @NotBlank(message = "State is required")
    private String state;
    @NotBlank(message = "Pincode is required")
    private String pincode;
    private BigDecimal lat;
    private BigDecimal lng;
    private Boolean isDefault = false;
}
