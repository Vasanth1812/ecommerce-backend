package com.fmcg.ecommerce.repository;

import com.fmcg.ecommerce.entity.CustomerNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerNoteRepository extends JpaRepository<CustomerNote, Long> {
    List<CustomerNote> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
