package com.fmcg.ecommerce.controller.admin;

import com.fmcg.ecommerce.common.ApiResponse;
import com.fmcg.ecommerce.common.PagedResponse;
import com.fmcg.ecommerce.dto.auth.UserSummaryDto;
import com.fmcg.ecommerce.entity.CustomerNote;
import com.fmcg.ecommerce.service.impl.AdminServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin - Dashboard & Customers", description = "Admin dashboard and customer management")
public class AdminController {

    private final AdminServiceImpl adminService;

    // ── Dashboard ─────────────────────────────────────────

    @GetMapping("/api/v1/admin/dashboard/overview")
    @Operation(summary = "Get dashboard overview stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(
            @RequestParam(defaultValue = "30d") String period) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getDashboardOverview(period)));
    }

    // ── Customers ─────────────────────────────────────────

    @GetMapping("/api/v1/admin/customers")
    @Operation(summary = "Get all customers with search and filter")
    public ResponseEntity<ApiResponse<PagedResponse<Map<String, Object>>>> getCustomers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(
                adminService.getCustomers(search, status,
                        PageRequest.of(page, size)))));
    }

    @GetMapping("/api/v1/admin/customers/{id}")
    @Operation(summary = "Get customer details by ID")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getCustomerById(id)));
    }

    @PatchMapping("/api/v1/admin/customers/{id}/status")
    @Operation(summary = "Update customer status (ACTIVE / BLOCKED)")
    public ResponseEntity<ApiResponse<UserSummaryDto>> updateStatus(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated",
                adminService.updateCustomerStatus(id, body.get("status"))));
    }

    @GetMapping("/api/v1/admin/customers/stats")
    @Operation(summary = "Get customer summary stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getCustomerStats()));
    }

    // ── Customer Notes ────────────────────────────────────

    @GetMapping("/api/v1/admin/customers/{id}/notes")
    @Operation(summary = "Admin: Get all notes for a customer")
    public ResponseEntity<ApiResponse<java.util.List<CustomerNote>>> getNotes(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getCustomerNotes(id)));
    }

    @PostMapping("/api/v1/admin/customers/{id}/notes")
    @Operation(summary = "Admin: Add a note to a customer")
    public ResponseEntity<ApiResponse<CustomerNote>> addNote(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("Note added",
                adminService.addCustomerNote(id, body.get("note"), auth.getName())));
    }

    @DeleteMapping("/api/v1/admin/customers/notes/{noteId}")
    @Operation(summary = "Admin: Delete a customer note")
    public ResponseEntity<ApiResponse<String>> deleteNote(
            @PathVariable Long noteId, Authentication auth) {
        adminService.deleteCustomerNote(noteId, auth.getName());
        return ResponseEntity.ok(ApiResponse.ok("Note deleted"));
    }
}
