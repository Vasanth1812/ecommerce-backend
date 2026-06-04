package com.fmcg.ecommerce.repository;

import com.fmcg.ecommerce.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findByUserId(Long userId);
    List<SupportTicket> findByStatus(String status);
}
