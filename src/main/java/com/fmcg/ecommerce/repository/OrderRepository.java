package com.fmcg.ecommerce.repository;

import com.fmcg.ecommerce.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query(value = "SELECT * FROM orders o WHERE o.user_id = :userId " +
           "AND ( CAST(:status AS text) IS NULL OR o.status = CAST(:status AS text) ) " +
           "ORDER BY o.created_at DESC",
           countQuery = "SELECT COUNT(*) FROM orders o WHERE o.user_id = :userId " +
           "AND ( CAST(:status AS text) IS NULL OR o.status = CAST(:status AS text) )",
           nativeQuery = true)
    Page<Order> findByUserIdAndStatus(@Param("userId") Long userId,
                                      @Param("status") String status,
                                      Pageable pageable);

    @Query(value = "SELECT o.* FROM orders o " +
           "JOIN users u ON o.user_id = u.id WHERE " +
           "( CAST(:status AS text) IS NULL OR o.status = CAST(:status AS text) ) " +
           "AND ( CAST(:from AS timestamp) IS NULL OR o.created_at >= CAST(:from AS timestamp) ) " +
           "AND ( CAST(:to AS timestamp)   IS NULL OR o.created_at <= CAST(:to AS timestamp) ) " +
           "AND ( CAST(:search AS text) IS NULL " +
           "  OR LOWER(u.name)          LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) " +
           "  OR LOWER(o.order_number)  LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) " +
           ") ORDER BY o.created_at DESC",
           countQuery = "SELECT COUNT(*) FROM orders o " +
           "JOIN users u ON o.user_id = u.id WHERE " +
           "( CAST(:status AS text) IS NULL OR o.status = CAST(:status AS text) ) " +
           "AND ( CAST(:from AS timestamp) IS NULL OR o.created_at >= CAST(:from AS timestamp) ) " +
           "AND ( CAST(:to AS timestamp)   IS NULL OR o.created_at <= CAST(:to AS timestamp) ) " +
           "AND ( CAST(:search AS text) IS NULL " +
           "  OR LOWER(u.name)          LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) " +
           "  OR LOWER(o.order_number)  LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) " +
           ")",
           nativeQuery = true)
    Page<Order> adminSearchOrders(@Param("status") String status,
                                   @Param("from") LocalDateTime from,
                                   @Param("to") LocalDateTime to,
                                   @Param("search") String search,
                                   Pageable pageable);

    long countByStatus(String status);
    long countByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.paymentStatus = 'PAID' " +
           "AND o.createdAt BETWEEN :from AND :to")
    BigDecimal sumRevenueBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.user.id = :userId AND o.paymentStatus = 'PAID'")
    BigDecimal sumTotalSpentByUser(@Param("userId") Long userId);

    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC LIMIT 10")
    java.util.List<Order> findTop10ByOrderByCreatedAtDesc();

    // Safe query — returns only user_id without triggering lazy-load chain
    @Query(value = "SELECT user_id FROM orders WHERE id = :orderId", nativeQuery = true)
    Long findUserIdByOrderId(@Param("orderId") Long orderId);
}
