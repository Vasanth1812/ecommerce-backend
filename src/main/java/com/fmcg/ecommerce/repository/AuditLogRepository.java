package com.fmcg.ecommerce.repository;

import com.fmcg.ecommerce.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByProductIdOrderByTimestampDesc(Long productId);
    Page<AuditLog> findByProductId(Long productId, Pageable pageable);
}
