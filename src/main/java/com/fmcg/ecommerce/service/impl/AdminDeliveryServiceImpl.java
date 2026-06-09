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
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    public List<DeliveryPartnerDto> getAllRiders() {
        return deliveryPartnerRepository.findAll().stream().map(d -> DeliveryPartnerDto.builder()
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
    public DeliveryPartnerDto onboardRider(String fullName, String phoneNumber, String vehicleType, String vehicleNumber, String zone, String status) {
        // Auto-create or fetch User
        User user = userRepository.findByMobile(phoneNumber).orElseGet(() -> {
            User newUser = User.builder()
                    .name(fullName)
                    .mobile(phoneNumber)
                    .email(phoneNumber + "@delivery.local") // dummy email since it might be required
                    .passwordHash(passwordEncoder.encode("123456"))
                    .role("DELIVERY_BOY")
                    .build();
            return userRepository.save(newUser);
        });

        // Ensure role is set
        if (!"DELIVERY_BOY".equals(user.getRole())) {
            user.setRole("DELIVERY_BOY");
            userRepository.save(user);
        }

        if (deliveryPartnerRepository.findByUserId(user.getId()).isPresent()) {
            throw new BadRequestException("User is already a delivery partner");
        }

        DeliveryPartner partner = DeliveryPartner.builder()
                .user(user)
                .vehicleType(vehicleType)
                .vehicleNumber(vehicleNumber)
                .zone(zone)
                .availabilityStatus(status != null && !status.isBlank() ? status : "FREE")
                .build();
        
        partner = deliveryPartnerRepository.save(partner);

        return DeliveryPartnerDto.builder()
                .id(partner.getId())
                .publicId(partner.getPublicId())
                .userId(partner.getUser().getId())
                .fullName(partner.getUser().getName())
                .phoneNumber(partner.getUser().getMobile())
                .vehicleType(partner.getVehicleType())
                .vehicleNumber(partner.getVehicleNumber())
                .zone(partner.getZone())
                .availabilityStatus(partner.getAvailabilityStatus())
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