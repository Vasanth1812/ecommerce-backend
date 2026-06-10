package com.fmcg.ecommerce.controller.admin;

import com.fmcg.ecommerce.common.ApiResponse;
import com.fmcg.ecommerce.dto.delivery.DeliveryBoyDto;
import com.fmcg.ecommerce.service.admin.AdminDeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Delivery & Logistics", description = "Delivery Partner Management and Live Tracking")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDeliveryController {

    private final AdminDeliveryService deliveryService;

    // --- Delivery Partners ---
    @GetMapping("/delivery/riders")
    @Operation(summary = "List all delivery boys and their current status")
    public ResponseEntity<ApiResponse<List<DeliveryBoyDto>>> getAllRiders() {
        return ResponseEntity.ok(ApiResponse.ok(deliveryService.getAllRiders()));
    }

    

    // --- Live Tracking ---
    @PutMapping("/delivery/live-tracking")
    @Operation(summary = "Update rider GPS coordinates")
    public ResponseEntity<ApiResponse<String>> updateLocation(@RequestBody Map<String, Object> payload) {
        Long riderId = Long.valueOf(payload.get("riderId").toString());
        Double lat = Double.valueOf(payload.get("lat").toString());
        Double lng = Double.valueOf(payload.get("lng").toString());
        deliveryService.updateRiderLocation(riderId, lat, lng);
        return ResponseEntity.ok(ApiResponse.ok("Location updated successfully"));
    }

    // --- Order Fulfillment ---
    @PostMapping("/orders/{id}/delivered")
    @Operation(summary = "Mark order as delivered and free up the delivery partner")
    public ResponseEntity<ApiResponse<Object>> markOrderDelivered(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(deliveryService.markOrderDelivered(id)));
    }
}
