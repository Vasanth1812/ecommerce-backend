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
                                 String movementType, String reason, String adminEmail, String expiryDate) {
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

    public List<Inventory> getOutOfStock() {
        return inventoryRepository.findOutOfStockItems();
    }

    // --- Phase 2: Inventory Workflows ---
    @Transactional
    public Warehouse createWarehouse(com.fmcg.ecommerce.dto.inventory.WarehouseDto dto) {
        Warehouse w = Warehouse.builder()
                .name(dto.getName())
                .type(dto.getType() != null ? dto.getType() : "WAREHOUSE")
                .address(dto.getAddress())
                .lat(dto.getLat())
                .lng(dto.getLng())
                .capacity(dto.getCapacity() != null ? dto.getCapacity() : 1000)
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .shortLocation(dto.getShortLocation())
                .city(dto.getCity())
                .state(dto.getState())
                .pincode(dto.getPincode())
                .usedCapacity(dto.getUsedCapacity() != null ? dto.getUsedCapacity() : 0)
                .staffCount(dto.getStaffCount())
                .operatingHours(dto.getOperatingHours())
                .managerName(dto.getManagerName())
                .contactNumber(dto.getContactNumber())
                .build();
        return warehouseRepository.save(w);
    }

    @Transactional
    public Warehouse updateWarehouse(Long id, com.fmcg.ecommerce.dto.inventory.WarehouseDto dto) {
        Warehouse w = warehouseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Warehouse", id));
        if (dto.getName() != null) w.setName(dto.getName());
        if (dto.getType() != null) w.setType(dto.getType());
        if (dto.getAddress() != null) w.setAddress(dto.getAddress());
        if (dto.getLat() != null) w.setLat(dto.getLat());
        if (dto.getLng() != null) w.setLng(dto.getLng());
        if (dto.getCapacity() != null) w.setCapacity(dto.getCapacity());
        if (dto.getIsActive() != null) w.setIsActive(dto.getIsActive());
        if (dto.getShortLocation() != null) w.setShortLocation(dto.getShortLocation());
        if (dto.getCity() != null) w.setCity(dto.getCity());
        if (dto.getState() != null) w.setState(dto.getState());
        if (dto.getPincode() != null) w.setPincode(dto.getPincode());
        if (dto.getUsedCapacity() != null) w.setUsedCapacity(dto.getUsedCapacity());
        if (dto.getStaffCount() != null) w.setStaffCount(dto.getStaffCount());
        if (dto.getOperatingHours() != null) w.setOperatingHours(dto.getOperatingHours());
        if (dto.getManagerName() != null) w.setManagerName(dto.getManagerName());
        if (dto.getContactNumber() != null) w.setContactNumber(dto.getContactNumber());
        return warehouseRepository.save(w);
    }

    @Transactional
    public com.fmcg.ecommerce.dto.inventory.StockTransferDto transferStock(com.fmcg.ecommerce.dto.inventory.StockTransferDto dto) {
        com.fmcg.ecommerce.repository.StockTransferRepository stockTransferRepo = 
            org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()
            .getAttribute("org.springframework.web.servlet.DispatcherServlet.CONTEXT", 0) != null ? 
            ((org.springframework.context.ApplicationContext)org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()
            .getAttribute("org.springframework.web.servlet.DispatcherServlet.CONTEXT", 0)).getBean(com.fmcg.ecommerce.repository.StockTransferRepository.class) : null;
            
        com.fmcg.ecommerce.entity.Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", dto.getProductId()));
        Warehouse from = warehouseRepository.findById(dto.getFromWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("From Warehouse", dto.getFromWarehouseId()));
        Warehouse to = warehouseRepository.findById(dto.getToWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("To Warehouse", dto.getToWarehouseId()));

        com.fmcg.ecommerce.entity.StockTransfer transfer = com.fmcg.ecommerce.entity.StockTransfer.builder()
                .transferNumber("TRF-" + System.currentTimeMillis())
                .product(product)
                .fromWarehouse(from)
                .toWarehouse(to)
                .quantity(dto.getQuantity())
                .status("PENDING")
                .notes(dto.getNotes())
                .build();

        if (stockTransferRepo != null) {
            transfer = stockTransferRepo.save(transfer);
        }
        
        return com.fmcg.ecommerce.dto.inventory.StockTransferDto.builder()
                .id(transfer.getId())
                .transferNumber(transfer.getTransferNumber())
                .productId(product.getId())
                .productName(product.getTitle())
                .fromWarehouseId(from.getId())
                .fromWarehouseName(from.getName())
                .toWarehouseId(to.getId())
                .toWarehouseName(to.getName())
                .quantity(transfer.getQuantity())
                .status(transfer.getStatus())
                .notes(transfer.getNotes())
                .createdAt(transfer.getCreatedAt())
                .completedAt(transfer.getCompletedAt())
                .build();
    }

    @Transactional
    public Inventory updateSafetyStock(Long inventoryId, Integer safetyStock) {
        Inventory inv = inventoryRepository.findById(inventoryId).orElseThrow(() -> new ResourceNotFoundException("Inventory", inventoryId));
        inv.setSafetyStock(safetyStock); // Store it in the safety_stock column
        inv.setReorderPoint(safetyStock); // Keep it identical to the reorder point so Low Stock alerts keep triggering correctly!
        return inventoryRepository.save(inv);
    }

    public Object getFefoData() {
        return List.of(Map.of("batchId", "B123", "expiryDate", "2024-12-01", "qty", 50, "action", "PRIORITY_DISPATCH"));
    }

    public Object getForecastData() {
        return List.of(Map.of("productId", 1, "predictedDepletionDays", 14, "suggestedReorderQty", 200));
    }

    public org.springframework.data.domain.Page<com.fmcg.ecommerce.entity.StockTransfer> getAllTransfers(org.springframework.data.domain.Pageable pageable) {
        com.fmcg.ecommerce.repository.StockTransferRepository stockTransferRepo = 
            (com.fmcg.ecommerce.repository.StockTransferRepository)((org.springframework.context.ApplicationContext)org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()
            .getAttribute("org.springframework.web.servlet.DispatcherServlet.CONTEXT", 0)).getBean(com.fmcg.ecommerce.repository.StockTransferRepository.class);
        return stockTransferRepo.findAll(pageable);
    }
}