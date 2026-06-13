package com.fmcg.ecommerce.repository;

import com.fmcg.ecommerce.entity.SavedPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedPaymentRepository extends JpaRepository<SavedPayment, Long> {
    List<SavedPayment> findByUserId(Long userId);
    Optional<SavedPayment> findByIdAndUserId(Long id, Long userId);
}
