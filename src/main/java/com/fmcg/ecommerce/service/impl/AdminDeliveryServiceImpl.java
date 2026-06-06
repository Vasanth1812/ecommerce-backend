package com.fmcg.ecommerce.service.impl;

import com.fmcg.ecommerce.dto.delivery.DeliveryPartnerDto;
import com.fmcg.ecommerce.entity.DeliveryPartner;
import com.fmcg.ecommerce.entity.Order;
import com.fmcg.ecommerce.entity.User;
import com.fmcg.ecommerce.exception.BadRequestException;
import com.fmcg.ecommerce.exception.ResourceNotFoundException;
import com.fmcg.ecommerce.repository.DeliveryPartnerRepository;
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

    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Override
    public List<DeliveryPartnerDto> getAllRiders() {
        List<User> deliveryBoys = userRepository.findAll().stream()
                .filter(u -> "DELIVERY_BOY".equals(u.getRole()))
                .collect(Collectors.toList());

        return deliveryBoys.stream().map(user -> {
            DeliveryPartner dp = deliveryPartnerRepository.findByUserId(user.getId()).orElse(null);
            return DeliveryPartnerDto.builder()
                    .id(dp != null ? dp.getId() : null)
                    .publicId(dp != null ? dp.getPublicId() : null)
                    .userId(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .phone(user.getMobile())
                    .vehicleType(dp != null ? dp.getVehicleType() : null)
                    .vehicleNumber(dp != null ? dp.getVehicleNumber() : null)
                    .availabilityStatus(dp != null ? dp.getAvailabilityStatus() : "OFFLINE")
                    .currentLat(dp != null ? dp.getCurrentLat() : null)
                    .currentLng(dp != null ? dp.getCurrentLng() : null)
                    .lastLocationUpdate(dp != null ? dp.getLastLocationUpdate() : null)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DeliveryPartnerDto onboardRider(Long userId, String vehicleType, String vehicleNumber) {
        if (deliveryPartnerRepository.findByUserId(userId).isPresent()) {
            throw new BadRequestException("User is already a delivery partner");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        
        // Ensure user has DELIVERY_BOY role (or add it)
        user.setRole("DELIVERY_BOY");
        userRepository.save(user);

        DeliveryPartner dp = DeliveryPartner.builder()
                .user(user)
                .vehicleType(vehicleType)
                .vehicleNumber(vehicleNumber)
                .availabilityStatus("FREE")
                .build();
        
        dp = deliveryPartnerRepository.save(dp);

        return DeliveryPartnerDto.builder()
                .id(dp.getId())
                .publicId(dp.getPublicId())
                .userId(dp.getUser().getId())
                .name(dp.getUser().getName())
                .email(dp.getUser().getEmail())
                .phone(dp.getUser().getMobile())
                .vehicleType(dp.getVehicleType())
                .vehicleNumber(dp.getVehicleNumber())
                .availabilityStatus(dp.getAvailabilityStatus())
                .build();
    }

    @Override
    @Transactional
    public void updateRiderLocation(Long riderId, Double lat, Double lng) {
        DeliveryPartner dp = deliveryPartnerRepository.findById(riderId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryPartner", riderId));
        
        dp.setCurrentLat(lat);
        dp.setCurrentLng(lng);
        dp.setLastLocationUpdate(LocalDateTime.now());
        
        deliveryPartnerRepository.save(dp);
    }

    @Override
    @Transactional
    public Object markOrderDelivered(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        
        if (order.getDeliveryPartner() == null) {
            throw new BadRequestException("Order does not have an assigned delivery partner");
        }

        order.setStatus("DELIVERED");
        orderRepository.save(order);

        DeliveryPartner dp = order.getDeliveryPartner();
        dp.setAvailabilityStatus("FREE");
        deliveryPartnerRepository.save(dp);

        return java.util.Map.of(
            "orderId", orderId, 
            "status", "DELIVERED", 
            "partnerId", dp.getId(), 
            "partnerStatus", "FREE"
        );
    }
}
