package com.fmcg.ecommerce.repository;

import com.fmcg.ecommerce.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProductId(Long productId);
    Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    @Query("SELECT i FROM Inventory i JOIN FETCH i.product p JOIN FETCH i.warehouse w " +
           "WHERE (:search IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%',:search,'%')) " +
           "  OR LOWER(p.sku) LIKE LOWER(CONCAT('%',:search,'%'))) " +
           "AND (:warehouseId IS NULL OR w.id = :warehouseId)")
    Page<Inventory> searchInventory(@Param("search") String search,
                                    @Param("warehouseId") Long warehouseId,
                                    Pageable pageable);

    @Query("SELECT i FROM Inventory i WHERE i.qtyAvailable <= i.reorderPoint AND i.qtyAvailable >= 0")
    List<Inventory> findLowStockItems();

    @Query("SELECT i FROM Inventory i WHERE i.qtyAvailable = 0")
    List<Inventory> findOutOfStockItems();

    long countByQtyAvailableLessThanEqual(int threshold);
}
