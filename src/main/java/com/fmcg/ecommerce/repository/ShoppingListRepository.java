package com.fmcg.ecommerce.repository;

import com.fmcg.ecommerce.entity.ShoppingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {
    List<ShoppingList> findByUserId(Long userId);
    Optional<ShoppingList> findByIdAndUserId(Long id, Long userId);
}
