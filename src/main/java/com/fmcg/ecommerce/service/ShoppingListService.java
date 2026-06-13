package com.fmcg.ecommerce.service;

import com.fmcg.ecommerce.dto.account.ShoppingListDto;

import java.util.List;

public interface ShoppingListService {
    List<ShoppingListDto.ListResponse> getShoppingLists(Long userId);
    ShoppingListDto.ListResponse createShoppingList(Long userId, ShoppingListDto.CreateListRequest request);
    ShoppingListDto.ListResponse addProductToList(Long userId, Long listId, ShoppingListDto.AddItemRequest request);
    void removeProductFromList(Long userId, Long listId, Long productId);
    void deleteShoppingList(Long userId, Long listId);
}
