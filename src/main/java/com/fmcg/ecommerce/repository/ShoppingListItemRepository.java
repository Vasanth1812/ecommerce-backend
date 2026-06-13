package com.fmcg.ecommerce.repository;

import com.fmcg.ecommerce.entity.ShoppingListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, Long> {
    Optional<ShoppingListItem> findByShoppingListIdAndProductId(Long listId, Long productId);
}
