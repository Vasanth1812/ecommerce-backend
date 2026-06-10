package com.fmcg.ecommerce.repository;

import com.fmcg.ecommerce.entity.DeliveryBoy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryBoyRepository extends JpaRepository<DeliveryBoy, Long> {
    Optional<DeliveryBoy> findByUserId(Long userId);
}
