package com.fmcg.ecommerce.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private Long id;
    private String label;
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String pincode;
    private BigDecimal lat;
    private BigDecimal lng;
    private boolean isDefault;
}
