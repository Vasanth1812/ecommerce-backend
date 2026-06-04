package com.fmcg.ecommerce.controller.admin;

import com.fmcg.ecommerce.common.ApiResponse;
import com.fmcg.ecommerce.dto.customer.CustomerAnalyticsDto;
import com.fmcg.ecommerce.dto.customer.SupportTicketDto;
import com.fmcg.ecommerce.entity.CustomerSegment;
import com.fmcg.ecommerce.entity.User;
import com.fmcg.ecommerce.service.admin.AdminCustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/customers")
@RequiredArgsConstructor
@Tag(name = "Admin Customers", description = "CRM & Marketing Operations")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCustomerController {

    private final AdminCustomerService customerService;

    // --- Segments ---
    @GetMapping("/segments")
    @Operation(summary = "List all dynamic customer groups")
    public ResponseEntity<ApiResponse<List<CustomerSegment>>> getSegments() {
        return ResponseEntity.ok(ApiResponse.ok(customerService.getAllSegments()));
    }

    @PostMapping("/segments")
    @Operation(summary = "Create a new customer segment")
    public ResponseEntity<ApiResponse<CustomerSegment>> createSegment(@RequestBody CustomerSegment segment) {
        return ResponseEntity.ok(ApiResponse.ok(customerService.createSegment(segment)));
    }

    @PutMapping("/segments/{id}")
    @Operation(summary = "Update segment criteria")
    public ResponseEntity<ApiResponse<CustomerSegment>> updateSegment(
            @PathVariable Long id, @RequestBody CustomerSegment segment) {
        return ResponseEntity.ok(ApiResponse.ok(customerService.updateSegment(id, segment)));
    }

    // --- Analytics ---
    @GetMapping("/analytics")
    @Operation(summary = "Get purchase frequency and behavior data")
    public ResponseEntity<ApiResponse<CustomerAnalyticsDto>> getCustomerAnalytics() {
        return ResponseEntity.ok(ApiResponse.ok(customerService.getCustomerAnalytics()));
    }

    // --- Support Tickets ---
    @GetMapping("/tickets")
    @Operation(summary = "List all support tickets")
    public ResponseEntity<ApiResponse<List<SupportTicketDto>>> getTickets() {
        return ResponseEntity.ok(ApiResponse.ok(customerService.getAllTickets()));
    }

    @PostMapping("/tickets")
    @Operation(summary = "Create a manual support ticket")
    public ResponseEntity<ApiResponse<SupportTicketDto>> createTicket(@RequestBody SupportTicketDto dto) {
        return ResponseEntity.ok(ApiResponse.ok(customerService.createTicket(dto)));
    }

    @PutMapping("/tickets/{id}")
    @Operation(summary = "Update ticket status")
    public ResponseEntity<ApiResponse<SupportTicketDto>> updateTicketStatus(
            @PathVariable Long id, @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        return ResponseEntity.ok(ApiResponse.ok(customerService.updateTicketStatus(id, status)));
    }

    // --- Fraud Alerts ---
    @GetMapping("/fraud-alerts")
    @Operation(summary = "Fetch users with high risk/fraud score")
    public ResponseEntity<ApiResponse<List<User>>> getFraudAlerts() {
        return ResponseEntity.ok(ApiResponse.ok(customerService.getHighRiskUsers()));
    }

    @PostMapping("/{id}/reset-fraud")
    @Operation(summary = "Reset a user's fraud score to 0")
    public ResponseEntity<ApiResponse<User>> resetFraudScore(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(customerService.resetFraudScore(id)));
    }
}
