package com.fmcg.ecommerce.controller;

import com.fmcg.ecommerce.common.ApiResponse;
import com.fmcg.ecommerce.common.PagedResponse;
import com.fmcg.ecommerce.dto.order.CreateOrderRequest;
import com.fmcg.ecommerce.dto.order.OrderResponse;
import com.fmcg.ecommerce.dto.order.ReorderResponse;
import com.fmcg.ecommerce.dto.order.UpdateOrderStatusRequest;
import com.fmcg.ecommerce.service.impl.OrderServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order placement and management")
public class OrderController {

    private final OrderServiceImpl orderService;

    private Long getUserId(Authentication auth) {
        return orderService.getUserIdByEmail(auth.getName());
    }

    // ── Customer Endpoints ────────────────────────────────

    @PostMapping("/api/v1/orders")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Place a new order (checkout)")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Order placed successfully", orderService.createOrder(getUserId(auth), request)));
    }

    @GetMapping("/api/v1/orders")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my orders")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> getMyOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        Page<OrderResponse> orders = orderService.getUserOrders(
                getUserId(auth), status, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(orders)));
    }

    @GetMapping("/api/v1/orders/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrderById(id, getUserId(auth))));
    }

    @GetMapping("/api/v1/orders/number/{orderNumber}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get order by order number")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrderByNumber(orderNumber)));
    }

    @PostMapping("/api/v1/orders/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel an order")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            Authentication auth) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(ApiResponse.ok("Order cancelled", orderService.cancelOrder(id, getUserId(auth), reason)));
    }

    @PostMapping("/api/v1/orders/{id}/reorder")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Reorder — adds all items from a past order back to cart")
    public ResponseEntity<ApiResponse<ReorderResponse>> reorder(
            @PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.reorder(id, getUserId(auth))));
    }

    // ── Admin Endpoints ───────────────────────────────────

    @GetMapping("/api/v1/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: List all orders with filters")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> adminGetOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<OrderResponse> orders = orderService.adminGetOrders(
                status, search, from, to, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(orders)));
    }

    @GetMapping("/api/v1/admin/orders/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Get order by ID")
    public ResponseEntity<ApiResponse<OrderResponse>> adminGetOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.adminGetOrderById(id)));
    }

    @PatchMapping("/api/v1/admin/orders/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Update order status")
    public ResponseEntity<ApiResponse<OrderResponse>> adminUpdateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated",
                orderService.adminUpdateStatus(id, request, auth.getName())));
    }

    // --- Phase 2: Order Fulfillment Workflows ---

    @GetMapping("/api/v1/admin/orders/{id}/timeline")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Retrieve order status history timeline")
    public ResponseEntity<ApiResponse<Object>> getOrderTimeline(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrderTimeline(id)));
    }

    @GetMapping("/api/v1/admin/orders/substitutions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Get list of order substitutions")
    public ResponseEntity<ApiResponse<PagedResponse<Object>>> getSubstitutions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        org.springframework.data.domain.Page<Object> emptyPage = 
            new org.springframework.data.domain.PageImpl<>(java.util.List.of(), org.springframework.data.domain.PageRequest.of(page, size), 0);
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(emptyPage)));
    }

    @GetMapping("/api/v1/admin/orders/bulk-jobs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Get list of bulk order processing jobs")
    public ResponseEntity<ApiResponse<PagedResponse<Object>>> getBulkJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        org.springframework.data.domain.Page<Object> emptyPage = 
            new org.springframework.data.domain.PageImpl<>(java.util.List.of(), org.springframework.data.domain.PageRequest.of(page, size), 0);
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(emptyPage)));
    }

    @PostMapping("/api/v1/admin/orders/{id}/assign-partner")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Assign delivery fleets/riders to an order")
    public ResponseEntity<ApiResponse<Object>> assignDeliveryPartner(
            @PathVariable Long id,
            @RequestParam String partnerId) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.assignDeliveryPartner(id, partnerId)));
    }

    @PostMapping("/api/v1/admin/orders/{id}/substitute")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Replace out-of-stock items in an order")
    public ResponseEntity<ApiResponse<Object>> substituteItem(
            @PathVariable Long id,
            @RequestParam Long oldProductId,
            @RequestParam Long newProductId) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.substituteOrderItem(id, oldProductId, newProductId)));
    }

    @PostMapping("/api/v1/admin/orders/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Bulk update multiple order statuses at once")
    public ResponseEntity<ApiResponse<Object>> bulkUpdateOrderStatus(
            @RequestBody java.util.List<Long> orderIds,
            @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.bulkUpdateOrderStatus(orderIds, status)));
    }
}
