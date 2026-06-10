package com.fmcg.ecommerce.service.delivery.impl;

import com.fmcg.ecommerce.dto.order.OrderResponse;
import com.fmcg.ecommerce.entity.DeliveryBoy;
import com.fmcg.ecommerce.entity.Order;
import com.fmcg.ecommerce.exception.ResourceNotFoundException;
import com.fmcg.ecommerce.repository.DeliveryBoyRepository;
import com.fmcg.ecommerce.repository.OrderRepository;
import com.fmcg.ecommerce.service.delivery.DeliveryBoyAppService;
import com.fmcg.ecommerce.service.impl.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import com.fmcg.ecommerce.service.impl.SseNotificationService;
import com.fmcg.ecommerce.entity.Notification;
import com.fmcg.ecommerce.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class DeliveryBoyAppServiceImpl implements DeliveryBoyAppService {

    private final DeliveryBoyRepository DeliveryBoyRepository;
    private final OrderRepository orderRepository;
    private final OrderServiceImpl orderService;
    private final SseNotificationService sseNotificationService;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAssignedOrders(Long userId) {
        DeliveryBoy boy = DeliveryBoyRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryBoy Profile", userId));

        // Fetch orders assigned to this boy that are NOT yet delivered or cancelled
        List<String> activeStatuses = List.of("PREPARING", "OUT_FOR_DELIVERY", "CONFIRMED");
        return orderRepository.findAll().stream()
                .filter(order -> order.getDeliveryBoy() != null 
                        && order.getDeliveryBoy().getId().equals(boy.getId())
                        && activeStatuses.contains(order.getStatus()))
                .map(orderService::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markOrderDelivered(Long userId, String orderPublicId) {
        DeliveryBoy boy = DeliveryBoyRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryBoy Profile", userId));

        // Note: For production, we'd add findByPublicId to OrderRepository. For now, filter via stream for simplicity.
        Order order = orderRepository.findAll().stream()
                .filter(o -> orderPublicId.equals(o.getPublicId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Order", "publicId", orderPublicId));

        if (order.getDeliveryBoy() == null || !order.getDeliveryBoy().getId().equals(boy.getId())) {
            throw new com.fmcg.ecommerce.exception.UnauthorizedException("This order is not assigned to you");
        }

        order.setStatus("DELIVERED");
        orderRepository.save(order);

        // Check if boy has any other active orders. If not, mark them FREE
        List<String> activeStatuses = List.of("PREPARING", "OUT_FOR_DELIVERY", "CONFIRMED");
        boolean hasMoreActiveOrders = orderRepository.findAll().stream()
                .anyMatch(o -> o.getDeliveryBoy() != null 
                        && o.getDeliveryBoy().getId().equals(boy.getId())
                        && activeStatuses.contains(o.getStatus())
                        && !o.getId().equals(order.getId()));

        if (!hasMoreActiveOrders) {
            boy.setAvailabilityStatus("FREE");
            DeliveryBoyRepository.save(boy);
        }
    }
}
