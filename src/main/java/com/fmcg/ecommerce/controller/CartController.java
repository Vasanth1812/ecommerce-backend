package com.fmcg.ecommerce.controller;

import com.fmcg.ecommerce.common.ApiResponse;
import com.fmcg.ecommerce.dto.cart.*;
import com.fmcg.ecommerce.service.impl.CartServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Cart", description = "Shopping cart management")
public class CartController {

    private final CartServiceImpl cartService;

    private Long getUserId(Authentication auth) {
        return cartService.getUserIdByEmail(auth.getName());
    }

    @GetMapping
    @Operation(summary = "Get current user's cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(cartService.getCart(getUserId(auth))));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @Valid @RequestBody AddToCartRequest request, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("Item added to cart", cartService.addItem(getUserId(auth), request)));
    }

    @PatchMapping("/items/{itemId}")
    @Operation(summary = "Update cart item quantity (0 = remove)")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(cartService.updateItem(getUserId(auth), itemId, request)));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @PathVariable Long itemId, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("Item removed", cartService.removeItem(getUserId(auth), itemId)));
    }

    @PostMapping("/apply-coupon")
    @Operation(summary = "Apply coupon code to cart")
    public ResponseEntity<ApiResponse<CartResponse>> applyCoupon(
            @Valid @RequestBody ApplyCouponRequest request, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("Coupon applied!", cartService.applyCoupon(getUserId(auth), request)));
    }

    @DeleteMapping("/remove-coupon")
    @Operation(summary = "Remove applied coupon from cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeCoupon(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("Coupon removed", cartService.removeCoupon(getUserId(auth))));
    }
}
