package com.fmcg.ecommerce.service;

import com.fmcg.ecommerce.dto.account.WishlistDto;

import java.util.List;

public interface WishlistService {
    List<WishlistDto.Response> getWishlist(Long userId);
    WishlistDto.Response addProductToWishlist(Long userId, Long productId);
    void removeProductFromWishlist(Long userId, Long productId);
}
