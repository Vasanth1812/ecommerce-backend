package com.fmcg.ecommerce.service.impl;

import com.fmcg.ecommerce.dto.account.WishlistDto;
import com.fmcg.ecommerce.entity.Inventory;
import com.fmcg.ecommerce.entity.Product;
import com.fmcg.ecommerce.entity.User;
import com.fmcg.ecommerce.entity.Wishlist;
import com.fmcg.ecommerce.exception.BadRequestException;
import com.fmcg.ecommerce.exception.ResourceNotFoundException;
import com.fmcg.ecommerce.repository.InventoryRepository;
import com.fmcg.ecommerce.repository.ProductRepository;
import com.fmcg.ecommerce.repository.UserRepository;
import com.fmcg.ecommerce.repository.WishlistRepository;
import com.fmcg.ecommerce.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<WishlistDto.Response> getWishlist(Long userId) {
        return wishlistRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WishlistDto.Response addProductToWishlist(Long userId, Long productId) {
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new BadRequestException("Product is already in your wishlist!");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .product(product)
                .build();

        Wishlist saved = wishlistRepository.save(wishlist);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void removeProductFromWishlist(Long userId, Long productId) {
        Wishlist wishlist = wishlistRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist Item not found"));
        wishlistRepository.delete(wishlist);
    }

    private WishlistDto.Response toResponse(Wishlist wishlist) {
        Product p = wishlist.getProduct();
        
        String stockStatus = inventoryRepository.findByProductId(p.getId())
                .map(inv -> inv.getQtyAvailable() > 0 ? "IN_STOCK" : "OUT_OF_STOCK")
                .orElse("OUT_OF_STOCK");

        String imageUrl = (p.getImages() != null && !p.getImages().isEmpty())
                ? p.getImages().stream().filter(i -> Boolean.TRUE.equals(i.getIsPrimary()))
                    .findFirst().orElse(p.getImages().get(0)).getUrl()
                : null;

        return WishlistDto.Response.builder()
                .id(wishlist.getId())
                .productId(p.getId())
                .productTitle(p.getTitle())
                .productSku(p.getSku())
                .imageUrl(imageUrl)
                .price(p.getPrice())
                .effectivePrice(p.getEffectivePrice())
                .stockStatus(stockStatus)
                .build();
    }
}
