package com.fmcg.ecommerce.controller.admin;

import com.fmcg.ecommerce.common.ApiResponse;
import com.fmcg.ecommerce.common.PagedResponse;
import com.fmcg.ecommerce.entity.Inventory;
import com.fmcg.ecommerce.entity.StockMovement;
import com.fmcg.ecommerce.entity.Warehouse;
import com.fmcg.ecommerce.service.impl.InventoryServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/inventory")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin - Inventory", description = "Inventory and stock management")
public class AdminInventoryController {

    private final InventoryServiceImpl inventoryService;

    @GetMapping
    @Operation(summary = "Get inventory list with search")
    public ResponseEntity<ApiResponse<PagedResponse<Inventory>>> getInventory(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(
                inventoryService.getInventory(search, warehouseId,
                        PageRequest.of(page, size, Sort.by("updatedAt").descending())))));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get inventory for a specific product")
    public ResponseEntity<ApiResponse<Inventory>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getInventoryByProduct(productId)));
    }

    @PostMapping("/adjust")
    @Operation(summary = "Adjust stock (IN / OUT / ADJUSTMENT)")
    public ResponseEntity<ApiResponse<Inventory>> adjustStock(
            @RequestBody Map<String, Object> payload,
            Authentication auth) {
        Long productId = payload.get("productId") != null ? Long.valueOf(payload.get("productId").toString()) : null;
        Long warehouseId = payload.get("warehouseId") != null ? Long.valueOf(payload.get("warehouseId").toString()) : null;
        Integer qty = payload.get("quantity") != null ? Integer.valueOf(payload.get("quantity").toString()) : null;
        String movementType = payload.get("type") != null ? payload.get("type").toString() : null;
        String reason = payload.get("reason") != null ? payload.get("reason").toString() : null;
        String expiryDate = payload.get("expiryDate") != null ? payload.get("expiryDate").toString() : null;
        
        Inventory updated = inventoryService.adjustStock(productId, warehouseId, qty, movementType, reason, auth.getName(), expiryDate);
        return ResponseEntity.ok(ApiResponse.ok("Stock adjusted successfully", updated));
    }

    @GetMapping("/warehouses")
    @Operation(summary = "Get all active warehouses")
    public ResponseEntity<ApiResponse<List<Warehouse>>> getWarehouses() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getWarehouses()));
    }

    @GetMapping("/product/{productId}/movements")
    @Operation(summary = "Get stock movement history for a product")
    public ResponseEntity<ApiResponse<PagedResponse<StockMovement>>> getMovements(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(
                inventoryService.getStockMovements(productId,
                        PageRequest.of(page, size, Sort.by("createdAt").descending())))));
    }

    @GetMapping("/report")
    @Operation(summary = "Get inventory summary report")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReport() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getInventoryReport()));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get all items currently in low stock")
    public ResponseEntity<ApiResponse<List<Inventory>>> getLowStock() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getLowStockItems()));
    }

    @GetMapping("/out-of-stock")
    @Operation(summary = "Get out of stock items")
    public ResponseEntity<ApiResponse<List<Inventory>>> getOutOfStock() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getOutOfStock()));
    }

    // --- Phase 2: Inventory Workflows ---

    @PostMapping("/warehouses")
    @Operation(summary = "Create a new warehouse location")
    public ResponseEntity<ApiResponse<Warehouse>> createWarehouse(@RequestBody com.fmcg.ecommerce.dto.inventory.WarehouseDto dto) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.createWarehouse(dto)));
    }

    @PutMapping("/warehouses/{id}")
    @Operation(summary = "Update warehouse details")
    public ResponseEntity<ApiResponse<Warehouse>> updateWarehouse(@PathVariable Long id, @RequestBody com.fmcg.ecommerce.dto.inventory.WarehouseDto dto) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.updateWarehouse(id, dto)));
    }

    @PostMapping("/transfers")
    @Operation(summary = "Log stock transfer between warehouses")
    public ResponseEntity<ApiResponse<com.fmcg.ecommerce.dto.inventory.StockTransferDto>> transferStock(@RequestBody com.fmcg.ecommerce.dto.inventory.StockTransferDto dto) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.transferStock(dto)));
    }

    @PutMapping("/safety-stock")
    @Operation(summary = "Adjust low-stock thresholds (safety stock) for an inventory item")
    public ResponseEntity<ApiResponse<Inventory>> updateSafetyStock(
            @RequestParam Long inventoryId,
            @RequestParam Integer safetyStock) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.updateSafetyStock(inventoryId, safetyStock)));
    }

    @GetMapping("/fefo")
    @Operation(summary = "Get First-Expired-First-Out batch expiry data")
    public ResponseEntity<ApiResponse<Object>> getFefoData() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getFefoData()));
    }

    @GetMapping("/forecast")
    @Operation(summary = "Get statistical stock depletion forecasting")
    public ResponseEntity<ApiResponse<Object>> getForecastData() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getForecastData()));
    }

    @GetMapping("/transfers")
    @Operation(summary = "Get list of all stock transfers")
    public ResponseEntity<ApiResponse<PagedResponse<com.fmcg.ecommerce.entity.StockTransfer>>> getAllTransfers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(
                inventoryService.getAllTransfers(PageRequest.of(page, size, Sort.by("createdAt").descending())))));
    }
}