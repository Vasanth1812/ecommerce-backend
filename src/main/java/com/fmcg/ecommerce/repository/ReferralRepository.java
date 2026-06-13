package com.fmcg.ecommerce.repository;

import com.fmcg.ecommerce.entity.Referral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReferralRepository extends JpaRepository<Referral, Long> {
    List<Referral> findByReferrerId(Long referrerId);
    Optional<Referral> findByReferredId(Long referredId);
    boolean existsByReferredId(Long referredId);
}
