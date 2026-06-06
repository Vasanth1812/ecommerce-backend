package com.fmcg.ecommerce.service.delivery;

import com.fmcg.ecommerce.dto.order.OrderResponse;
import java.util.List;

public interface DeliveryPartnerAppService {
    List<OrderResponse> getAssignedOrders(Long userId);
    void markOrderDelivered(Long userId, String orderPublicId);
}
