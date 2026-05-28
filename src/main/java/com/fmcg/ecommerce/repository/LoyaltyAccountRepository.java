package com.fmcg.ecommerce.repository;

import com.fmcg.ecommerce.entity.LoyaltyAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, Long> {
    Optional<LoyaltyAccount> findByUserId(Long userId);
}
