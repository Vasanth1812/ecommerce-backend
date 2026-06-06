package com.fmcg.ecommerce.repository;

import com.fmcg.ecommerce.entity.AbTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AbTestRepository extends JpaRepository<AbTest, Long> {
    List<AbTest> findByStatus(String status);
}
