package com.fmcg.ecommerce.service.admin;

import com.fmcg.ecommerce.dto.delivery.DeliveryBoyDto;

import java.util.List;

public interface AdminDeliveryService {
    List<DeliveryBoyDto> getAllRiders();
        
    void updateRiderLocation(Long riderId, Double lat, Double lng);
    Object markOrderDelivered(Long orderId);
}
