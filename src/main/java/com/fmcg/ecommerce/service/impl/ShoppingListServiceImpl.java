package com.fmcg.ecommerce.service.impl;

import com.fmcg.ecommerce.dto.account.ShoppingListDto;
import com.fmcg.ecommerce.entity.Product;
import com.fmcg.ecommerce.entity.ShoppingList;
import com.fmcg.ecommerce.entity.ShoppingListItem;
import com.fmcg.ecommerce.entity.User;
import com.fmcg.ecommerce.exception.BadRequestException;
import com.fmcg.ecommerce.exception.ResourceNotFoundException;
import com.fmcg.ecommerce.repository.ProductRepository;
import com.fmcg.ecommerce.repository.ShoppingListItemRepository;
import com.fmcg.ecommerce.repository.ShoppingListRepository;
import com.fmcg.ecommerce.repository.UserRepository;
import com.fmcg.ecommerce.service.ShoppingListService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShoppingListServiceImpl implements ShoppingListService {

    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingListItemRepository shoppingListItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ShoppingListDto.ListResponse> getShoppingLists(Long userId) {
        return shoppingListRepository.findByUserId(userId).stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ShoppingListDto.ListResponse createShoppingList(Long userId, ShoppingListDto.CreateListRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        ShoppingList list = ShoppingList.builder()
                .user(user)
                .name(request.getName())
                .build();

        return toListResponse(shoppingListRepository.save(list));
    }

    @Override
    @Transactional
    public ShoppingListDto.ListResponse addProductToList(Long userId, Long listId, ShoppingListDto.AddItemRequest request) {
        ShoppingList list = shoppingListRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Shopping List", listId));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

        ShoppingListItem item = shoppingListItemRepository.findByShoppingListIdAndProductId(listId, request.getProductId())
                .orElse(ShoppingListItem.builder()
                        .shoppingList(list)
                        .product(product)
                        .qty(0)
                        .build());

        item.setQty(item.getQty() + request.getQty());
        shoppingListItemRepository.save(item);

        return toListResponse(shoppingListRepository.findById(listId).get());
    }

    @Override
    @Transactional
    public void removeProductFromList(Long userId, Long listId, Long productId) {
        ShoppingList list = shoppingListRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Shopping List", listId));

        ShoppingListItem item = shoppingListItemRepository.findByShoppingListIdAndProductId(listId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Shopping List Item not found"));

        shoppingListItemRepository.delete(item);
    }

    @Override
    @Transactional
    public void deleteShoppingList(Long userId, Long listId) {
        ShoppingList list = shoppingListRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Shopping List", listId));
        shoppingListRepository.delete(list);
    }

    private ShoppingListDto.ListResponse toListResponse(ShoppingList list) {
        List<ShoppingListDto.ItemResponse> items = list.getItems().stream().map(i -> {
            Product p = i.getProduct();
            String imageUrl = (p.getImages() != null && !p.getImages().isEmpty())
                    ? p.getImages().stream().filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                        .findFirst().orElse(p.getImages().get(0)).getUrl()
                    : null;

            return ShoppingListDto.ItemResponse.builder()
                    .id(i.getId())
                    .productId(p.getId())
                    .productTitle(p.getTitle())
                    .imageUrl(imageUrl)
                    .price(p.getEffectivePrice())
                    .qty(i.getQty())
                    .build();
        }).collect(Collectors.toList());

        return ShoppingListDto.ListResponse.builder()
                .id(list.getId())
                .name(list.getName())
                .createdAt(list.getCreatedAt())
                .items(items)
                .build();
    }
}
