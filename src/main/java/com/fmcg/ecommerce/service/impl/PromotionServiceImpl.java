package com.fmcg.ecommerce.service.impl;

import com.fmcg.ecommerce.entity.Coupon;
import com.fmcg.ecommerce.entity.Notification;
import com.fmcg.ecommerce.entity.Promotion;
import com.fmcg.ecommerce.entity.User;
import com.fmcg.ecommerce.exception.BadRequestException;
import com.fmcg.ecommerce.exception.ResourceNotFoundException;
import com.fmcg.ecommerce.repository.CouponRepository;
import com.fmcg.ecommerce.repository.NotificationRepository;
import com.fmcg.ecommerce.repository.PromotionRepository;
import com.fmcg.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl {

    private final PromotionRepository promotionRepository;
    private final CouponRepository couponRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // ── Promotions ────────────────────────────────────────

    public List<Promotion> getActivePromotions() {
        return promotionRepository.findActivePromotions(LocalDateTime.now());
    }

    public Page<Promotion> getAllPromotions(Pageable pageable) {
        return promotionRepository.findAll(pageable);
    }

    @Transactional
    public Promotion createPromotion(Promotion promotion) {
        return promotionRepository.save(promotion);
    }

    @Transactional
    public Promotion updatePromotion(Long id, Promotion updated) {
        Promotion p = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", id));
        p.setName(updated.getName());
        p.setDescription(updated.getDescription());
        p.setType(updated.getType());
        p.setDiscountValue(updated.getDiscountValue());
        p.setDiscountType(updated.getDiscountType());
        p.setStartDate(updated.getStartDate());
        p.setEndDate(updated.getEndDate());
        p.setStatus(updated.getStatus());
        p.setMinOrder(updated.getMinOrder());
        p.setMaxUses(updated.getMaxUses());
        return promotionRepository.save(p);
    }

    @Transactional
    public void deletePromotion(Long id) {
        if (!promotionRepository.existsById(id)) throw new ResourceNotFoundException("Promotion", id);
        promotionRepository.deleteById(id);
    }

    // ── Coupons ───────────────────────────────────────────

    public Page<Coupon> getCoupons(Pageable pageable) {
        return couponRepository.findAll(pageable);
    }

    @Transactional
    public Coupon createCoupon(Coupon coupon) {
        if (couponRepository.existsByCode(coupon.getCode()))
            throw new BadRequestException("Coupon code '" + coupon.getCode() + "' already exists");
        return couponRepository.save(coupon);
    }

    @Transactional
    public Coupon updateCoupon(Long id, Coupon updated) {
        Coupon c = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", id));
        if (!c.getCode().equals(updated.getCode()) && couponRepository.existsByCodeAndIdNot(updated.getCode(), id))
            throw new BadRequestException("Coupon code already in use");
        c.setCode(updated.getCode());
        c.setType(updated.getType());
        c.setDiscountValue(updated.getDiscountValue());
        c.setMinOrder(updated.getMinOrder());
        c.setMaxUses(updated.getMaxUses());
        c.setValidFrom(updated.getValidFrom());
        c.setValidUntil(updated.getValidUntil());
        c.setIsActive(updated.getIsActive());
        return couponRepository.save(c);
    }

    @Transactional
    public void deleteCoupon(Long id) {
        if (!couponRepository.existsById(id)) throw new ResourceNotFoundException("Coupon", id);
        couponRepository.deleteById(id);
    }

    public Map<String, Object> validateCoupon(String code, BigDecimal cartTotal) {
        Coupon coupon = couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new BadRequestException("Coupon not found"));
        LocalDateTime now = LocalDateTime.now();
        if (!Boolean.TRUE.equals(coupon.getIsActive()))
            return Map.of("valid", false, "message", "Coupon is inactive");
        if (coupon.getValidUntil() != null && now.isAfter(coupon.getValidUntil()))
            return Map.of("valid", false, "message", "Coupon has expired");
        if (coupon.getMinOrder() != null && cartTotal.compareTo(coupon.getMinOrder()) < 0)
            return Map.of("valid", false, "message", "Minimum order ₹" + coupon.getMinOrder() + " required");
        if (coupon.getMaxUses() != null && coupon.getMaxUses() > 0 && coupon.getUsedCount() >= coupon.getMaxUses())
            return Map.of("valid", false, "message", "Coupon limit reached");

        BigDecimal discount = switch (coupon.getType()) {
            case "PERCENTAGE" -> cartTotal.multiply(coupon.getDiscountValue().divide(BigDecimal.valueOf(100)));
            case "FIXED" -> coupon.getDiscountValue().min(cartTotal);
            case "FREE_DELIVERY" -> new BigDecimal("49");
            default -> BigDecimal.ZERO;
        };
        return Map.of("valid", true, "message", "Coupon applied!", "discount", discount,
                "newTotal", cartTotal.subtract(discount).max(BigDecimal.ZERO));
    }

    // ── Notifications ─────────────────────────────────────

    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    @Transactional
    public void createNotification(Long userId, String title, String message, String type, String referenceId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;
        notificationRepository.save(Notification.builder()
                .user(user).title(title).message(message)
                .type(type).referenceId(referenceId).isRead(false)
                .build());
    }
}
