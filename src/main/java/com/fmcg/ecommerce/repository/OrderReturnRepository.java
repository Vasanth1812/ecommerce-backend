package com.fmcg.ecommerce.repository;

import com.fmcg.ecommerce.entity.OrderReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderReturnRepository extends JpaRepository<OrderReturn, Long> {
    List<OrderReturn> findByUserId(Long userId);
    Optional<OrderReturn> findByIdAndUserId(Long id, Long userId);
}
