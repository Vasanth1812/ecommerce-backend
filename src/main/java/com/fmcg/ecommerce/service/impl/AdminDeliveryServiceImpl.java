package com.fmcg.ecommerce.service.impl;

import com.fmcg.ecommerce.dto.delivery.DeliveryBoyDto;
import com.fmcg.ecommerce.entity.DeliveryBoy;
import com.fmcg.ecommerce.entity.Order;
import com.fmcg.ecommerce.entity.User;
import com.fmcg.ecommerce.exception.BadRequestException;
import com.fmcg.ecommerce.exception.ResourceNotFoundException;
import com.fmcg.ecommerce.repository.DeliveryBoyRepository;
import com.fmcg.ecommerce.repository.OrderRepository;
import com.fmcg.ecommerce.repository.UserRepository;
import com.fmcg.ecommerce.service.admin.AdminDeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDeliveryServiceImpl implements AdminDeliveryService {

    private final DeliveryBoyRepository DeliveryBoyRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    public List<DeliveryBoyDto> getAllRiders() {
        return DeliveryBoyRepository.findAll().stream().map(d -> DeliveryBoyDto.builder()
                .id(d.getId())
                .publicId(d.getPublicId())
                .userId(d.getUser() != null ? d.getUser().getId() : null)
                .fullName(d.getUser() != null ? d.getUser().getName() : null)
                .phoneNumber(d.getUser() != null ? d.getUser().getMobile() : null)
                .vehicleType(d.getVehicleType())
                .vehicleNumber(d.getVehicleNumber())
                .zone(d.getZone())
                .availabilityStatus(d.getAvailabilityStatus())
                .currentLat(d.getCurrentLat())
                .currentLng(d.getCurrentLng())
                .build()).collect(Collectors.toList());
    }

    

    @Override
    @Transactional
    public void updateRiderLocation(Long riderId, Double lat, Double lng) {
        DeliveryBoy dp = DeliveryBoyRepository.findById(riderId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryBoy", riderId));
        
        dp.setCurrentLat(lat);
        dp.setCurrentLng(lng);
        dp.setLastLocationUpdate(LocalDateTime.now());
        
        DeliveryBoyRepository.save(dp);
    }

    @Override
    @Transactional
    public Object markOrderDelivered(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        
        if (order.getDeliveryBoy() == null) {
            throw new BadRequestException("Order does not have an assigned delivery partner");
        }

        order.setStatus("DELIVERED");
        orderRepository.save(order);

        DeliveryBoy dp = order.getDeliveryBoy();
        dp.setAvailabilityStatus("FREE");
        DeliveryBoyRepository.save(dp);

        return java.util.Map.of(
            "orderId", orderId, 
            "status", "DELIVERED", 
            "partnerId", dp.getId(), 
            "partnerStatus", "FREE"
        );
    }
}