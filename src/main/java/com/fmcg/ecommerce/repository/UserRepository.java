package com.fmcg.ecommerce.repository;

import com.fmcg.ecommerce.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByMobile(String mobile);
    boolean existsByEmail(String email);
    
    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
    boolean existsByMobile(String mobile);
    long countByRole(String role);
    long countByStatus(String status);

    @Query(value = "SELECT * FROM users u WHERE u.role = 'CUSTOMER' AND ( " +
           "  CAST(:search AS text) IS NULL " +
           "  OR LOWER(u.name)   LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) " +
           "  OR LOWER(u.email)  LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) " +
           "  OR u.mobile        LIKE        CONCAT('%', CAST(:search AS text), '%') " +
           ")",
           countQuery = "SELECT COUNT(*) FROM users u WHERE u.role = 'CUSTOMER' AND ( " +
           "  CAST(:search AS text) IS NULL " +
           "  OR LOWER(u.name)   LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) " +
           "  OR LOWER(u.email)  LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) " +
           "  OR u.mobile        LIKE        CONCAT('%', CAST(:search AS text), '%') " +
           ")",
           nativeQuery = true)
    Page<User> searchCustomers(@Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT o.user.id) FROM Order o WHERE o.createdAt >= :from")
    long countActiveCustomers(@Param("from") java.time.LocalDateTime from);
}
