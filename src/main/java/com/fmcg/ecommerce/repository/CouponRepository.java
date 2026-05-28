package com.fmcg.ecommerce.repository;

import com.fmcg.ecommerce.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCode(String code);
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, Long id);

    @Query("SELECT c FROM Coupon c WHERE c.isActive = true " +
           "AND c.validFrom <= :now AND c.validUntil >= :now")
    List<Coupon> findActiveCoupons(@Param("now") LocalDateTime now);
}
