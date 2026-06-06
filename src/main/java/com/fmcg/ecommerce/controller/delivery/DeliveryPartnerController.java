package com.fmcg.ecommerce.controller.delivery;

import com.fmcg.ecommerce.common.ApiResponse;
import com.fmcg.ecommerce.dto.order.OrderResponse;
import com.fmcg.ecommerce.service.delivery.DeliveryPartnerAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
@Tag(name = "Delivery Partner App", description = "APIs for the Delivery Boy Mobile App")
@PreAuthorize("hasRole('DELIVERY_BOY')")
public class DeliveryPartnerController {

    private final DeliveryPartnerAppService deliveryAppService;
    private final com.fmcg.ecommerce.repository.UserRepository userRepository;

    private Long getUserId(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new com.fmcg.ecommerce.exception.ResourceNotFoundException("User", "email", auth.getName()))
                .getId();
    }

    @GetMapping("/orders/assigned")
    @Operation(summary = "Get current assigned orders for the logged-in delivery boy")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAssignedOrders(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("Assigned orders fetched successfully", deliveryAppService.getAssignedOrders(getUserId(auth))));
    }

    @PostMapping("/orders/{orderPublicId}/deliver")
    @Operation(summary = "Mark an order as delivered and free up the delivery partner")
    public ResponseEntity<ApiResponse<Object>> markOrderDelivered(
            @PathVariable String orderPublicId, Authentication auth) {
        deliveryAppService.markOrderDelivered(getUserId(auth), orderPublicId);
        return ResponseEntity.ok(ApiResponse.ok("Order marked as delivered successfully!"));
    }
}
