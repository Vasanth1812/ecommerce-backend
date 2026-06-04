package com.fmcg.ecommerce.controller.admin;

import com.fmcg.ecommerce.common.ApiResponse;
import com.fmcg.ecommerce.dto.dashboard.DashboardOverviewDto;
import com.fmcg.ecommerce.service.admin.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard", description = "APIs for dashboard metrics and analytics")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @GetMapping("/overview")
    @Operation(summary = "Get core metrics (Revenue, Orders, Customers count)")
    public ResponseEntity<ApiResponse<DashboardOverviewDto>> getOverview(
            @RequestParam(defaultValue = "30d") String period) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getOverview(period)));
    }

    @GetMapping("/live-orders")
    @Operation(summary = "Get real-time active orders")
    public ResponseEntity<ApiResponse<Object>> getLiveOrders() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getOverview("30d").getLiveOrders()));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock alerts")
    public ResponseEntity<ApiResponse<Object>> getLowStock() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getOverview("30d").getLowStockAlerts()));
    }

    @GetMapping("/vendor-payments")
    @Operation(summary = "Get upcoming vendor payout queue")
    public ResponseEntity<ApiResponse<Object>> getVendorPayments() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getOverview("30d").getUpcomingPayments()));
    }

    @GetMapping("/top-products")
    @Operation(summary = "Get best-selling products by volume/revenue")
    public ResponseEntity<ApiResponse<Object>> getTopProducts() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getOverview("30d").getTopProducts()));
    }

    @GetMapping("/acquisition")
    @Operation(summary = "Get user acquisition sources and metrics")
    public ResponseEntity<ApiResponse<Object>> getAcquisition() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getOverview("30d").getAcquisitionMetrics()));
    }
}
