package com.fmcg.ecommerce.repository;

import com.fmcg.ecommerce.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    List<Warehouse> findByIsActiveTrueOrderByNameAsc();
}
