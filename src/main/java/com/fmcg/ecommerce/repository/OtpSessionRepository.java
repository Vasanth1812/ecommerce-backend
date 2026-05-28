package com.fmcg.ecommerce.repository;

import com.fmcg.ecommerce.entity.OtpSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpSessionRepository extends JpaRepository<OtpSession, Long> {

    @Query("SELECT o FROM OtpSession o WHERE o.identifier = :identifier AND o.used = false " +
           "ORDER BY o.createdAt DESC LIMIT 1")
    Optional<OtpSession> findLatestByIdentifier(@Param("identifier") String identifier);

    @Modifying
    @Query("DELETE FROM OtpSession o WHERE o.expiresAt < :now")
    void deleteExpired(@Param("now") LocalDateTime now);
}
