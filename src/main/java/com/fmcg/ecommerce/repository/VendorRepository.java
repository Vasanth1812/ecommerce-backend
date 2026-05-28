package com.fmcg.ecommerce.repository;

import com.fmcg.ecommerce.entity.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {
    
    @Query("SELECT v FROM Vendor v WHERE " +
           "(:search IS NULL OR LOWER(v.businessName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(v.contactName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(v.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR v.phone LIKE CONCAT('%', :search, '%')) " +
           "AND (:status IS NULL OR v.status = :status)")
    Page<Vendor> searchVendors(@Param("search") String search, 
                               @Param("status") String status, 
                               Pageable pageable);
}
