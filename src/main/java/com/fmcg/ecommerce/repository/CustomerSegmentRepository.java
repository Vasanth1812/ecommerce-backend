package com.fmcg.ecommerce.repository;

import com.fmcg.ecommerce.entity.CustomerSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerSegmentRepository extends JpaRepository<CustomerSegment, Long> {
}
