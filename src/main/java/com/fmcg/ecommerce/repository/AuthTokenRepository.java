package com.fmcg.ecommerce.repository;

import com.fmcg.ecommerce.entity.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {
    Optional<AuthToken> findByRefreshTokenAndRevokedFalse(String refreshToken);

    @Modifying
    @Query("UPDATE AuthToken t SET t.revoked = true WHERE t.user.id = :userId")
    void revokeAllUserTokens(@Param("userId") Long userId);
}
