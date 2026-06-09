package com.fmcg.ecommerce.repository;

import com.fmcg.ecommerce.entity.BulkJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BulkJobRepository extends JpaRepository<BulkJob, Long> {
    Optional<BulkJob> findByPublicId(String publicId);
}