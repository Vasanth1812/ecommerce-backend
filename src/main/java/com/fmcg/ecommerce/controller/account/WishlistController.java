package com.fmcg.ecommerce.controller.account;

import com.fmcg.ecommerce.common.ApiResponse;
import com.fmcg.ecommerce.dto.account.WishlistDto;
import com.fmcg.ecommerce.entity.User;
import com.fmcg.ecommerce.repository.UserRepository;
import com.fmcg.ecommerce.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
@Tag(name = "Customer - Wishlist", description = "APIs for managing user wishlists")
@SecurityRequirement(name = "bearerAuth")
public class WishlistController {

    private final WishlistService wishlistService;
    private final UserRepository userRepository;

    private Long getUserId(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    @GetMapping
    @Operation(summary = "Get current user's wishlist")
    public ResponseEntity<ApiResponse<List<WishlistDto.Response>>> getWishlist(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(wishlistService.getWishlist(getUserId(authentication))));
    }

    @PostMapping
    @Operation(summary = "Add product to wishlist")
    public ResponseEntity<ApiResponse<WishlistDto.Response>> addToWishlist(
            Authentication authentication,
            @RequestBody WishlistDto.Request request) {
        return ResponseEntity.ok(ApiResponse.ok("Product added to wishlist", 
                wishlistService.addProductToWishlist(getUserId(authentication), request.getProductId())));
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Remove product from wishlist")
    public ResponseEntity<ApiResponse<String>> removeFromWishlist(
            Authentication authentication,
            @PathVariable Long productId) {
        wishlistService.removeProductFromWishlist(getUserId(authentication), productId);
        return ResponseEntity.ok(ApiResponse.ok("Product removed from wishlist"));
    }
}
