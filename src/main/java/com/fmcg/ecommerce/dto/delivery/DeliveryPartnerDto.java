package com.fmcg.ecommerce.dto.delivery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPartnerDto {
    private Long id;
    private String publicId;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String vehicleType;
    private String vehicleNumber;
    private String availabilityStatus;
    private Double currentLat;
    private Double currentLng;
    private LocalDateTime lastLocationUpdate;
}
