package com.fmcg.ecommerce.repository;

import com.fmcg.ecommerce.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentIdIsNullAndIsActiveTrueOrderBySortOrderAsc();
    List<Category> findByParentIdAndIsActiveTrueOrderBySortOrderAsc(Long parentId);
    List<Category> findByIsActiveTrueOrderBySortOrderAsc();
    Optional<Category> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id);
    long countByParentId(Long parentId);
}
