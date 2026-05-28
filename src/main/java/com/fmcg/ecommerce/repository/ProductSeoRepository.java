package com.fmcg.ecommerce.repository;

import com.fmcg.ecommerce.entity.ProductSeo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductSeoRepository extends JpaRepository<ProductSeo, Long> {
    Optional<ProductSeo> findByProductId(Long productId);
    boolean existsBySlug(String slug);
}
