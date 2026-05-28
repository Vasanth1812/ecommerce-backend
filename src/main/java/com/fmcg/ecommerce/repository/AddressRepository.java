package com.fmcg.ecommerce.repository;

import com.fmcg.ecommerce.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserId(Long userId);
    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);
    long countByUserId(Long userId);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
    void unsetAllDefaults(@Param("userId") Long userId);

    // Safe query — returns only user_id without loading the full User entity
    @Query(value = "SELECT user_id FROM addresses WHERE id = :addressId", nativeQuery = true)
    Long findUserIdByAddressId(@Param("addressId") Long addressId);
}
