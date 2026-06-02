package com.fmcg.ecommerce.controller.admin;

import com.fmcg.ecommerce.common.ApiResponse;
import com.fmcg.ecommerce.common.PagedResponse;
import com.fmcg.ecommerce.dto.vendor.VendorRequest;
import com.fmcg.ecommerce.dto.vendor.VendorResponse;
import com.fmcg.ecommerce.service.impl.VendorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/vendors")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Vendors", description = "Vendor management for marketplace")
public class AdminVendorController {

    private final VendorService vendorService;

    @GetMapping
    @Operation(summary = "Get all vendors with pagination and filters")
    public ResponseEntity<ApiResponse<PagedResponse<VendorResponse>>> getVendors(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<VendorResponse> vendors = vendorService.getVendors(search, status, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(vendors)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vendor by ID")
    public ResponseEntity<ApiResponse<VendorResponse>> getVendor(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(vendorService.getVendor(id)));
    }

    @PostMapping
    @Operation(summary = "Create a new vendor")
    public ResponseEntity<ApiResponse<VendorResponse>> createVendor(@Valid @RequestBody VendorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Vendor created successfully", vendorService.createVendor(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vendor details")
    public ResponseEntity<ApiResponse<VendorResponse>> updateVendor(
            @PathVariable Long id, @Valid @RequestBody VendorRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Vendor updated successfully", vendorService.updateVendor(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a vendor")
    public ResponseEntity<ApiResponse<String>> deleteVendor(@PathVariable Long id) {
        vendorService.deleteVendor(id);
        return ResponseEntity.ok(ApiResponse.ok("Vendor deleted successfully"));
    }

    @GetMapping("/settlements")
    @Operation(summary = "Get vendor settlements (Stub)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSettlements() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "This is a basic stub implementation. Full settlement flow requires the payment module.");
        data.put("pendingAmount", 12500.50);
        data.put("paidAmount", 45000.00);
        data.put("settlements", List.of());
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve a pending vendor")
    public ResponseEntity<ApiResponse<VendorResponse>> approveVendor(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Vendor approved successfully", vendorService.approveVendor(id)));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject a pending vendor")
    public ResponseEntity<ApiResponse<VendorResponse>> rejectVendor(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Vendor rejected successfully", vendorService.rejectVendor(id)));
    }
}
