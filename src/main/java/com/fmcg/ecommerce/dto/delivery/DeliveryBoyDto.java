package com.fmcg.ecommerce.dto.delivery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryBoyDto {
    private Long id;
    private String publicId;
    private Long userId;
    private String fullName;
    private String phoneNumber;
    private String vehicleType;
    private String vehicleNumber;
    private String zone;
    private String availabilityStatus;
    private Double currentLat;
    private Double currentLng;
}