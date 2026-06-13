package com.fmcg.ecommerce.service.impl;

import com.fmcg.ecommerce.dto.cart.*;
import com.fmcg.ecommerce.entity.*;
import com.fmcg.ecommerce.exception.BadRequestException;
import com.fmcg.ecommerce.exception.ResourceNotFoundException;
import com.fmcg.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final CouponUsageRepository couponUsageRepository;

    private static final BigDecimal FREE_DELIVERY_THRESHOLD = new BigDecimal("499");
    private static final BigDecimal DELIVERY_FEE = new BigDecimal("49");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.05");

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) return emptyCart();
        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse addItem(Long userId, AddToCartRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

        if (!"ACTIVE".equals(product.getStatus())) {
            throw new BadRequestException("Product '" + product.getTitle() + "' is not available");
        }

        // Check stock
        Optional<Inventory> inv = inventoryRepository.findByProductId(product.getId());
        int available = inv.map(Inventory::getQtyAvailable).orElse(0);
        if (available < request.getQty()) {
            throw new BadRequestException("Only " + available + " unit(s) available in stock");
        }

        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart newCart = Cart.builder().user(user).couponDiscount(BigDecimal.ZERO).build();
            return cartRepository.save(newCart);
        });

        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQty = item.getQty() + request.getQty();
            if (newQty > available) throw new BadRequestException("Cannot add more. Only " + available + " available.");
            item.setQty(newQty);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .sku(product.getSku())
                    .qty(request.getQty())
                    .unitPrice(product.getPrice())
                    .build();
            cartItemRepository.save(newItem);
        }

        Cart updated = cartRepository.findById(cart.getId()).orElseThrow();
        return buildCartResponse(updated);
    }

    @Transactional
    public CartResponse updateItem(Long userId, Long itemId, UpdateCartItemRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Item does not belong to your cart");
        }

        if (request.getQty() == 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQty(request.getQty());
            cartItemRepository.save(item);
        }

        Cart updated = cartRepository.findById(cart.getId()).orElseThrow();
        return buildCartResponse(updated);
    }

    @Transactional
    public CartResponse removeItem(Long userId, Long itemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", itemId));
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Item does not belong to your cart");
        }
        cartItemRepository.delete(item);
        Cart updated = cartRepository.findById(cart.getId()).orElseThrow();
        return buildCartResponse(updated);
    }

    @Transactional
    public CartResponse applyCoupon(Long userId, ApplyCouponRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        Coupon coupon = couponRepository.findByCode(request.getCode().toUpperCase())
                .orElseThrow(() -> new BadRequestException("Coupon '" + request.getCode() + "' not found"));

        if (!Boolean.TRUE.equals(coupon.getIsActive())) throw new BadRequestException("Coupon is not active");
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getValidFrom() != null && now.isBefore(coupon.getValidFrom()))
            throw new BadRequestException("Coupon is not yet valid");
        if (coupon.getValidUntil() != null && now.isAfter(coupon.getValidUntil()))
            throw new BadRequestException("Coupon has expired");
        if (coupon.getMaxUses() != null && coupon.getMaxUses() > 0 && coupon.getUsedCount() >= coupon.getMaxUses())
            throw new BadRequestException("Coupon usage limit reached");

        if (couponUsageRepository.existsByUserIdAndCouponId(userId, coupon.getId())) {
            throw new BadRequestException("You have already used this coupon");
        }

        BigDecimal subtotal = calculateSubtotal(cart);
        if (coupon.getMinOrder() != null && subtotal.compareTo(coupon.getMinOrder()) < 0) {
            throw new BadRequestException("Minimum order of â‚¹" + coupon.getMinOrder() + " required for this coupon");
        }

        BigDecimal discount = calculateCouponDiscount(coupon, subtotal);
        cart.setAppliedCoupon(coupon);
        cart.setCouponDiscount(discount);
        cartRepository.save(cart);

        Cart updated = cartRepository.findById(cart.getId()).orElseThrow();
        return buildCartResponse(updated);
    }

    @Transactional
    public CartResponse removeCoupon(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        cart.setAppliedCoupon(null);
        cart.setCouponDiscount(BigDecimal.ZERO);
        cartRepository.save(cart);
        Cart updated = cartRepository.findById(cart.getId()).orElseThrow();
        return buildCartResponse(updated);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cartItemRepository.deleteByCartId(cart.getId());
            cart.setAppliedCoupon(null);
            cart.setCouponDiscount(BigDecimal.ZERO);
            cartRepository.save(cart);
        });
    }

    // â”€â”€ Helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private CartResponse buildCartResponse(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        List<CartItemResponse> itemResponses = items.stream().map(item -> {
            Product p = item.getProduct();
            Optional<Inventory> inv = inventoryRepository.findByProductId(p.getId());
            String stockStatus = inv.map(i -> i.getQtyAvailable() > 0 ? "IN_STOCK" : "OUT_OF_STOCK").orElse("OUT_OF_STOCK");
            int maxQty = inv.map(Inventory::getQtyAvailable).orElse(0);

            String imageUrl = (p.getImages() != null && !p.getImages().isEmpty())
                    ? p.getImages().stream().filter(i -> Boolean.TRUE.equals(i.getIsPrimary()))
                        .findFirst().orElse(p.getImages().get(0)).getUrl()
                    : null;

            BigDecimal finalDiscountPrice = null;
            String finalPromotionName = null;
            BigDecimal effectiveUnitPrice = item.getUnitPrice();

            if (p.getPromotions() != null && !p.getPromotions().isEmpty()) {
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                BigDecimal lowestPrice = p.getPrice();

                for (com.fmcg.ecommerce.entity.Promotion promo : p.getPromotions()) {
                    if ("ACTIVE".equals(promo.getStatus()) &&
                            (promo.getStartDate() == null || !now.isBefore(promo.getStartDate())) &&
                            (promo.getEndDate() == null || !now.isAfter(promo.getEndDate()))) {

                        BigDecimal candidatePrice = p.getPrice();
                        if ("PERCENTAGE".equals(promo.getDiscountType()) && promo.getDiscountValue() != null) {
                            BigDecimal discountAmount = p.getPrice().multiply(promo.getDiscountValue().divide(BigDecimal.valueOf(100)));
                            candidatePrice = p.getPrice().subtract(discountAmount);
                        } else if ("FIXED".equals(promo.getDiscountType()) && promo.getDiscountValue() != null) {
                            candidatePrice = p.getPrice().subtract(promo.getDiscountValue());
                        }

                        if (candidatePrice.compareTo(BigDecimal.ZERO) < 0) {
                            candidatePrice = BigDecimal.ZERO;
                        }

                        if (candidatePrice.compareTo(lowestPrice) < 0) {
                            lowestPrice = candidatePrice;
                            finalDiscountPrice = candidatePrice;
                            finalPromotionName = promo.getName();
                            effectiveUnitPrice = candidatePrice;
                        }
                    }
                }
            }

            BigDecimal itemTotal;
            if (Boolean.TRUE.equals(p.getIsBogoActive())) {
                int chargeableQty = item.getQty() - (item.getQty() / 2);
                itemTotal = effectiveUnitPrice.multiply(BigDecimal.valueOf(chargeableQty));
            } else {
                itemTotal = effectiveUnitPrice.multiply(BigDecimal.valueOf(item.getQty()));
            }

            return CartItemResponse.builder()
                    .id(item.getId())
                    .productId(p.getId())
                    .title(p.getTitle())
                    .sku(p.getSku())
                    .imageUrl(imageUrl)
                    .brand(p.getBrand())
                    .unit(p.getUnit())
                    .qty(item.getQty())
                    .unitPrice(effectiveUnitPrice)
                    .discountPrice(finalDiscountPrice)
                    .activePromotionName(finalPromotionName)
                    .mrp(p.getMrp())
                    .total(itemTotal)
                    .stockStatus(stockStatus)
                    .maxQty(maxQty)
                    .isBogoActive(p.getIsBogoActive())
                    .build();
        }).collect(Collectors.toList());

        BigDecimal subtotal = itemResponses.stream()
                .map(CartItemResponse::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal couponDiscount = cart.getCouponDiscount() != null ? cart.getCouponDiscount() : BigDecimal.ZERO;
        BigDecimal deliveryFee = subtotal.compareTo(FREE_DELIVERY_THRESHOLD) >= 0 ? BigDecimal.ZERO : DELIVERY_FEE;
        BigDecimal taxable = subtotal.subtract(couponDiscount);
        BigDecimal tax = taxable.compareTo(BigDecimal.ZERO) > 0 ? taxable.multiply(TAX_RATE) : BigDecimal.ZERO;
        BigDecimal total = taxable.add(deliveryFee).add(tax);

        String couponCode = cart.getAppliedCoupon() != null ? cart.getAppliedCoupon().getCode() : null;

        return CartResponse.builder()
                .id(cart.getId())
                .items(itemResponses)
                .itemCount(itemResponses.stream().mapToInt(CartItemResponse::getQty).sum())
                .subtotal(subtotal)
                .couponDiscount(couponDiscount)
                .deliveryFee(deliveryFee)
                .tax(tax)
                .total(total.max(BigDecimal.ZERO))
                .couponCode(couponCode)
                .freeDelivery(deliveryFee.compareTo(BigDecimal.ZERO) == 0)
                .minForFreeDelivery(FREE_DELIVERY_THRESHOLD)
                .build();
    }

    private BigDecimal calculateSubtotal(Cart cart) {
        return cartItemRepository.findByCartId(cart.getId()).stream()
                .map(i -> {
                    int chargeableQty = i.getQty();
                    if (Boolean.TRUE.equals(i.getProduct().getIsBogoActive())) {
                        chargeableQty = i.getQty() - (i.getQty() / 2);
                    }
                    return i.getProduct().getEffectivePrice().multiply(BigDecimal.valueOf(chargeableQty));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateCouponDiscount(Coupon coupon, BigDecimal subtotal) {
        return switch (coupon.getType()) {
            case "PERCENTAGE" -> subtotal.multiply(coupon.getDiscountValue().divide(BigDecimal.valueOf(100)));
            case "FIXED" -> coupon.getDiscountValue().min(subtotal);
            case "FREE_DELIVERY" -> DELIVERY_FEE;
            default -> BigDecimal.ZERO;
        };
    }

    private CartResponse emptyCart() {
        return CartResponse.builder()
                .items(new ArrayList<>())
                .itemCount(0)
                .subtotal(BigDecimal.ZERO)
                .couponDiscount(BigDecimal.ZERO)
                .deliveryFee(DELIVERY_FEE)
                .tax(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .freeDelivery(false)
                .minForFreeDelivery(FREE_DELIVERY_THRESHOLD)
                .build();
    }

    public Long getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .or(() -> userRepository.findByMobile(email))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }
    @Transactional(readOnly = true)
    public List<CouponResponse> getAvailableCoupons(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<Coupon> allCoupons = couponRepository.findAll();
        
        return allCoupons.stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
                .filter(c -> c.getValidFrom() == null || !now.isBefore(c.getValidFrom()))
                .filter(c -> c.getValidUntil() == null || !now.isAfter(c.getValidUntil()))
                .filter(c -> c.getMaxUses() == null || c.getMaxUses() <= 0 || c.getUsedCount() < c.getMaxUses())
                .filter(c -> !couponUsageRepository.existsByUserIdAndCouponId(userId, c.getId()))
                .map(c -> {
                    String description;
                    if (c.getMinOrder() != null && c.getMinOrder().compareTo(BigDecimal.ZERO) > 0) {
                        description = "This coupon requires minimum order ₹" + c.getMinOrder() + " for this coupon " + c.getCode();
                    } else {
                        description = "Apply " + c.getCode() + " for a discount!";
                    }
                    return CouponResponse.builder()
                            .code(c.getCode())
                            .type(c.getType())
                            .discountValue(c.getDiscountValue())
                            .discountType(c.getDiscountType())
                            .minOrder(c.getMinOrder())
                            .description(description)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
