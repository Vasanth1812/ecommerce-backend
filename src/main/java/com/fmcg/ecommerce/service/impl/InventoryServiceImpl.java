package com.fmcg.ecommerce.service.impl;

import com.fmcg.ecommerce.entity.Inventory;
import com.fmcg.ecommerce.entity.StockMovement;
import com.fmcg.ecommerce.entity.Warehouse;
import com.fmcg.ecommerce.exception.BadRequestException;
import com.fmcg.ecommerce.exception.ResourceNotFoundException;
import com.fmcg.ecommerce.repository.InventoryRepository;
import com.fmcg.ecommerce.repository.ProductRepository;
import com.fmcg.ecommerce.repository.StockMovementRepository;
import com.fmcg.ecommerce.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl {

    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public Page<Inventory> getInventory(String search, Long warehouseId, Pageable pageable) {
        // Prevent PostgreSQL "lower(bytea) does not exist" error by using empty string instead of null for JPQL LOWER()
        String safeSearch = search != null ? search : "";
        return inventoryRepository.searchInventory(safeSearch, warehouseId, pageable);
    }

    public Inventory getInventoryByProduct(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product: " + productId));
    }

    @Transactional
    public Inventory adjustStock(Long productId, Long warehouseId, Integer qty,
                                  String movementType, String reason, String adminEmail) {
        if (!productRepository.existsById(productId))
            throw new ResourceNotFoundException("Product", productId);
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", warehouseId));

        Inventory inv = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseGet(() -> {
                    var product = productRepository.findById(productId).orElseThrow();
                    return Inventory.builder().product(product).warehouse(warehouse)
                            .qtyAvailable(0).qtyReserved(0).reorderPoint(10).safetyStock(0).build();
                });

        switch (movementType.toUpperCase()) {
            case "IN" -> inv.setQtyAvailable(inv.getQtyAvailable() + qty);
            case "OUT" -> {
                if (inv.getQtyAvailable() < qty)
                    throw new BadRequestException("Insufficient stock. Available: " + inv.getQtyAvailable());
                inv.setQtyAvailable(inv.getQtyAvailable() - qty);
            }
            case "ADJUSTMENT" -> inv.setQtyAvailable(qty);
            default -> throw new BadRequestException("Invalid movement type: " + movementType);
        }

        Inventory saved = inventoryRepository.save(inv);

        StockMovement movement = StockMovement.builder()
                .product(saved.getProduct()).warehouse(warehouse)
                .movementType(movementType.toUpperCase())
                .qty(qty).reason(reason).createdBy(adminEmail)
                .build();
        stockMovementRepository.save(movement);

        return saved;
    }

    public List<Warehouse> getWarehouses() {
        return warehouseRepository.findByIsActiveTrueOrderByNameAsc();
    }

    public Page<StockMovement> getStockMovements(Long productId, Pageable pageable) {
        return stockMovementRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable);
    }

    public Map<String, Object> getInventoryReport() {
        Map<String, Object> report = new HashMap<>();
        long total = inventoryRepository.count();
        List<Inventory> lowStock = inventoryRepository.findLowStockItems();
        List<Inventory> outOfStock = inventoryRepository.findOutOfStockItems();
        report.put("totalProducts", total);
        report.put("lowStockCount", lowStock.size());
        report.put("outOfStockCount", outOfStock.size());
        report.put("inStockCount", total - outOfStock.size());
        return report;
    }

    public List<Inventory> getLowStockItems() {
        return inventoryRepository.findLowStockItems();
    }

    public List<Inventory> getOutOfStockItems() {
        return inventoryRepository.findOutOfStockItems();
    }
}
