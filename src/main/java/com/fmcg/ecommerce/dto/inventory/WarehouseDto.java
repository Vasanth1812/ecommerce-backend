package com.fmcg.ecommerce.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseDto {
    private Long id;
    private String name;
    private String type;
    private String address;
    private BigDecimal lat;
    private BigDecimal lng;
    private Integer capacity;
    
    // New fields mapped from UI
    private Boolean isActive;
    private String shortLocation;
    private String city;
    private String state;
    private String pincode;
    private Integer usedCapacity;
    private Integer staffCount;
    private String operatingHours;
    private String managerName;
    private String contactNumber;
}
