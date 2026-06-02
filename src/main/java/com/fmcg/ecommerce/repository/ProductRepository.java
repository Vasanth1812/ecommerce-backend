package com.fmcg.ecommerce.repository;

import com.fmcg.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
    Optional<Product> findByBarcode(String barcode);
    boolean existsBySku(String sku);
    boolean existsBySkuAndIdNot(String sku, Long id);
    int countByCategoryId(Long categoryId);

    @Query(value = "SELECT * FROM products p WHERE " +
           "( CAST(:search AS text) IS NULL " +
           "  OR LOWER(p.title)   LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) " +
           "  OR LOWER(p.brand)   LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) " +
           "  OR LOWER(p.sku)     LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) " +
           "  OR LOWER(p.barcode) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) " +
           ") " +
           "AND ( CAST(:status AS text)   IS NULL OR p.status      = CAST(:status AS text) ) " +
           "AND ( CAST(:categoryId AS bigint) IS NULL OR p.category_id = CAST(:categoryId AS bigint) ) " +
           "AND ( CAST(:brand AS text)    IS NULL OR LOWER(p.brand) LIKE LOWER(CONCAT('%', CAST(:brand AS text), '%')) ) " +
           "AND ( CAST(:minPrice AS numeric) IS NULL OR p.price >= CAST(:minPrice AS numeric) ) " +
           "AND ( CAST(:maxPrice AS numeric) IS NULL OR p.price <= CAST(:maxPrice AS numeric) ) " +
           "ORDER BY p.created_at DESC, p.id DESC",
           countQuery = "SELECT COUNT(*) FROM products p WHERE " +
           "( CAST(:search AS text) IS NULL " +
           "  OR LOWER(p.title)   LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) " +
           "  OR LOWER(p.brand)   LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) " +
           "  OR LOWER(p.sku)     LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) " +
           "  OR LOWER(p.barcode) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) " +
           ") " +
           "AND ( CAST(:status AS text)   IS NULL OR p.status      = CAST(:status AS text) ) " +
           "AND ( CAST(:categoryId AS bigint) IS NULL OR p.category_id = CAST(:categoryId AS bigint) ) " +
           "AND ( CAST(:brand AS text)    IS NULL OR LOWER(p.brand) LIKE LOWER(CONCAT('%', CAST(:brand AS text), '%')) ) " +
           "AND ( CAST(:minPrice AS numeric) IS NULL OR p.price >= CAST(:minPrice AS numeric) ) " +
           "AND ( CAST(:maxPrice AS numeric) IS NULL OR p.price <= CAST(:maxPrice AS numeric) )",
           nativeQuery = true)
    Page<Product> searchProducts(
            @Param("search") String search,
            @Param("status") String status,
            @Param("categoryId") Long categoryId,
            @Param("brand") String brand,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    List<Product> findByIdIn(List<Long> ids);

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.status = 'ACTIVE'")
    List<Product> findActiveByCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.brand IS NOT NULL ORDER BY p.brand")
    List<String> findDistinctBrands();
}
