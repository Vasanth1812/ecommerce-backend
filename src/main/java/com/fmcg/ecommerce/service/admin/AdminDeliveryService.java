package com.fmcg.ecommerce.service.admin;

import com.fmcg.ecommerce.dto.delivery.DeliveryPartnerDto;

import java.util.List;

public interface AdminDeliveryService {
    List<DeliveryPartnerDto> getAllRiders();
    DeliveryPartnerDto onboardRider(Long userId, String vehicleType, String vehicleNumber);
    void updateRiderLocation(Long riderId, Double lat, Double lng);
    Object markOrderDelivered(Long orderId);
}
