package com.fmcg.ecommerce.controller.admin;

import com.fmcg.ecommerce.common.ApiResponse;
import com.fmcg.ecommerce.common.PagedResponse;
import com.fmcg.ecommerce.dto.report.ReportDTO.*;
import com.fmcg.ecommerce.service.impl.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin - Reports", description = "All reporting and analytics endpoints")
public class ReportController {

    private final ReportService reportService;

    // --- Vendors ---
    @GetMapping("/vendors")
    @Operation(summary = "Get vendor performance reports")
    public ResponseEntity<ApiResponse<PagedResponse<VendorReportEntry>>> getVendorReports(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<VendorReportEntry> result = reportService.getVendorReports(search, PageRequest.of(page > 0 ? page - 1 : 0, pageSize));
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(result)));
    }

    @GetMapping("/vendors/summary")
    @Operation(summary = "Get vendor summary statistics")
    public ResponseEntity<ApiResponse<VendorReportSummary>> getVendorSummary() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getVendorSummary()));
    }

    // --- Taxes ---
    @GetMapping("/taxes")
    @Operation(summary = "Get tax reports")
    public ResponseEntity<ApiResponse<PagedResponse<TaxReportEntry>>> getTaxReports(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<TaxReportEntry> result = reportService.getTaxReports(search, PageRequest.of(page > 0 ? page - 1 : 0, pageSize));
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(result)));
    }

    @GetMapping("/taxes/summary")
    @Operation(summary = "Get tax summary statistics")
    public ResponseEntity<ApiResponse<TaxReportSummary>> getTaxSummary() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getTaxSummary()));
    }

    // --- Customers ---
    @GetMapping("/customers")
    @Operation(summary = "Get customer reports")
    public ResponseEntity<ApiResponse<PagedResponse<CustomerReportEntry>>> getCustomerReports(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<CustomerReportEntry> result = reportService.getCustomerReports(search, PageRequest.of(page > 0 ? page - 1 : 0, pageSize));
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(result)));
    }

    @GetMapping("/customers/summary")
    @Operation(summary = "Get customer summary statistics")
    public ResponseEntity<ApiResponse<CustomerSummary>> getCustomerSummary() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getCustomerSummary()));
    }

    // --- GST ---
    @GetMapping("/gst")
    @Operation(summary = "Get GST reports")
    public ResponseEntity<ApiResponse<PagedResponse<GSTReportEntry>>> getGSTReports(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<GSTReportEntry> result = reportService.getGSTReports(search, PageRequest.of(page > 0 ? page - 1 : 0, pageSize));
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(result)));
    }

    @GetMapping("/gst/summary")
    @Operation(summary = "Get GST summary statistics")
    public ResponseEntity<ApiResponse<GSTSummary>> getGSTSummary() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getGSTSummary()));
    }

    // --- Cohorts ---
    @GetMapping("/cohorts")
    @Operation(summary = "Get cohort analysis data")
    public ResponseEntity<ApiResponse<PagedResponse<CohortEntry>>> getCohortData(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<CohortEntry> result = reportService.getCohortData(PageRequest.of(page > 0 ? page - 1 : 0, pageSize));
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(result)));
    }

    @GetMapping("/cohorts/summary")
    @Operation(summary = "Get cohort summary statistics")
    public ResponseEntity<ApiResponse<CohortSummary>> getCohortSummary() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getCohortSummary()));
    }

    // --- Abandoned Cart ---
    @GetMapping("/abandoned-carts")
    @Operation(summary = "Get abandoned cart reports")
    public ResponseEntity<ApiResponse<PagedResponse<AbandonedCartEntry>>> getAbandonedCartData(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<AbandonedCartEntry> result = reportService.getAbandonedCartData(search, PageRequest.of(page > 0 ? page - 1 : 0, pageSize));
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(result)));
    }

    @GetMapping("/abandoned-carts/summary")
    @Operation(summary = "Get abandoned cart summary statistics")
    public ResponseEntity<ApiResponse<AbandonedCartSummary>> getAbandonedCartSummary() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getAbandonedCartSummary()));
    }

    // --- Revenue Analytics ---
    @GetMapping("/revenue")
    @Operation(summary = "Get revenue analytics reports")
    public ResponseEntity<ApiResponse<PagedResponse<RevenueAnalyticsEntry>>> getRevenueAnalytics(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<RevenueAnalyticsEntry> result = reportService.getRevenueAnalytics(PageRequest.of(page > 0 ? page - 1 : 0, pageSize));
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(result)));
    }

    @GetMapping("/revenue/summary")
    @Operation(summary = "Get revenue summary statistics")
    public ResponseEntity<ApiResponse<RevenueSummary>> getRevenueSummary() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getRevenueSummary()));
    }

    // --- Promotions ---
    @GetMapping("/promotions")
    @Operation(summary = "Get promotion ROI reports")
    public ResponseEntity<ApiResponse<PagedResponse<PromotionROIEntry>>> getPromotionReports(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<PromotionROIEntry> result = reportService.getPromotionReports(search, PageRequest.of(page > 0 ? page - 1 : 0, pageSize));
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(result)));
    }

    @GetMapping("/promotions/summary")
    @Operation(summary = "Get promotion summary statistics")
    public ResponseEntity<ApiResponse<PromotionROISummary>> getPromotionSummary() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getPromotionSummary()));
    }

    // --- Sales ---
    @GetMapping("/sales/summary")
    @Operation(summary = "Get sales overview statistics")
    public ResponseEntity<ApiResponse<SalesSummary>> getSalesSummary(
            @RequestParam(defaultValue = "30d") String period) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getSalesSummary(period)));
    }

    // --- Inventory ---
    @GetMapping("/inventory")
    @Operation(summary = "Get detailed inventory valuation and status")
    public ResponseEntity<ApiResponse<PagedResponse<InventoryReportEntry>>> getInventoryReport(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<InventoryReportEntry> result = reportService.getInventoryReport(search, PageRequest.of(page > 0 ? page - 1 : 0, pageSize));
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(result)));
    }

    @GetMapping("/inventory/summary")
    @Operation(summary = "Get inventory summary statistics")
    public ResponseEntity<ApiResponse<InventorySummary>> getInventorySummary() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getInventorySummary()));
    }

    // --- Export ---
    @GetMapping("/{type}/export")
    @Operation(summary = "Export a specific report to CSV or Excel")
    public ResponseEntity<byte[]> exportReport(
            @org.springframework.web.bind.annotation.PathVariable String type,
            @RequestParam(defaultValue = "csv") String format) {
        byte[] fileBytes = reportService.exportReport(type, format);
        
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + type + "_report." + format);
        headers.set(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/octet-stream");
        
        return new ResponseEntity<>(fileBytes, headers, org.springframework.http.HttpStatus.OK);
    }
}
