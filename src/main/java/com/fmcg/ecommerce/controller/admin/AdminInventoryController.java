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
            @RequestParam Long productId,
            @RequestParam Long warehouseId,
            @RequestParam Integer qty,
            @RequestParam String movementType,
            @RequestParam(required = false) String reason,
            Authentication auth) {
        Inventory updated = inventoryService.adjustStock(productId, warehouseId, qty, movementType, reason, auth.getName());
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
    @Operation(summary = "Get all items currently out of stock")
    public ResponseEntity<ApiResponse<List<Inventory>>> getOutOfStock() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getOutOfStockItems()));
    }
}
