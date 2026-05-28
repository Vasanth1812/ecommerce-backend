package com.fmcg.ecommerce.controller.admin;

import com.fmcg.ecommerce.common.ApiResponse;
import com.fmcg.ecommerce.common.PagedResponse;
import com.fmcg.ecommerce.entity.Coupon;
import com.fmcg.ecommerce.entity.Notification;
import com.fmcg.ecommerce.entity.Promotion;
import com.fmcg.ecommerce.repository.UserRepository;
import com.fmcg.ecommerce.service.impl.PromotionServiceImpl;
import com.fmcg.ecommerce.service.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin - Promotions & Notifications", description = "Promotions, coupons and notification management")
public class AdminPromotionController {

    private final PromotionServiceImpl promotionService;
    private final UserRepository userRepository;

    // ── Promotions ────────────────────────────────────────

    @GetMapping("/api/v1/promotions")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Get active promotions (public)")
    public ResponseEntity<ApiResponse<List<Promotion>>> getActivePromotions() {
        return ResponseEntity.ok(ApiResponse.ok(promotionService.getActivePromotions()));
    }

    @PostMapping("/api/v1/promotions/validate")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Validate a coupon code")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateCoupon(
            @RequestParam String code, @RequestParam BigDecimal cartTotal) {
        return ResponseEntity.ok(ApiResponse.ok(promotionService.validateCoupon(code, cartTotal)));
    }

    @GetMapping("/api/v1/admin/promotions")
    @Operation(summary = "Admin: List all promotions")
    public ResponseEntity<ApiResponse<PagedResponse<Promotion>>> getAllPromotions(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(
                promotionService.getAllPromotions(PageRequest.of(page, size, Sort.by("createdAt").descending())))));
    }

    @PostMapping("/api/v1/admin/promotions")
    @Operation(summary = "Admin: Create promotion")
    public ResponseEntity<ApiResponse<Promotion>> createPromotion(@RequestBody Promotion promotion) {
        return ResponseEntity.ok(ApiResponse.ok("Promotion created", promotionService.createPromotion(promotion)));
    }

    @PutMapping("/api/v1/admin/promotions/{id}")
    @Operation(summary = "Admin: Update promotion")
    public ResponseEntity<ApiResponse<Promotion>> updatePromotion(
            @PathVariable Long id, @RequestBody Promotion promotion) {
        return ResponseEntity.ok(ApiResponse.ok("Promotion updated", promotionService.updatePromotion(id, promotion)));
    }

    @DeleteMapping("/api/v1/admin/promotions/{id}")
    @Operation(summary = "Admin: Delete promotion")
    public ResponseEntity<ApiResponse<String>> deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.ok(ApiResponse.ok("Promotion deleted"));
    }

    // ── Coupons ───────────────────────────────────────────

    @GetMapping("/api/v1/admin/promotions/coupons")
    @Operation(summary = "Admin: List all coupons")
    public ResponseEntity<ApiResponse<PagedResponse<Coupon>>> getCoupons(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(
                promotionService.getCoupons(PageRequest.of(page, size, Sort.by("createdAt").descending())))));
    }

    @PostMapping("/api/v1/admin/promotions/coupons")
    @Operation(summary = "Admin: Create coupon")
    public ResponseEntity<ApiResponse<Coupon>> createCoupon(@RequestBody Coupon coupon) {
        return ResponseEntity.ok(ApiResponse.ok("Coupon created", promotionService.createCoupon(coupon)));
    }

    @PutMapping("/api/v1/admin/promotions/coupons/{id}")
    @Operation(summary = "Admin: Update coupon")
    public ResponseEntity<ApiResponse<Coupon>> updateCoupon(@PathVariable Long id, @RequestBody Coupon coupon) {
        return ResponseEntity.ok(ApiResponse.ok("Coupon updated", promotionService.updateCoupon(id, coupon)));
    }

    @DeleteMapping("/api/v1/admin/promotions/coupons/{id}")
    @Operation(summary = "Admin: Delete coupon")
    public ResponseEntity<ApiResponse<String>> deleteCoupon(@PathVariable Long id) {
        promotionService.deleteCoupon(id);
        return ResponseEntity.ok(ApiResponse.ok("Coupon deleted"));
    }

    // ── Notifications ─────────────────────────────────────

    @GetMapping("/api/v1/notifications")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my notifications")
    public ResponseEntity<ApiResponse<PagedResponse<Notification>>> getNotifications(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = userRepository.findByEmail(auth.getName())
                .or(() -> userRepository.findByMobile(auth.getName()))
                .map(u -> u.getId()).orElse(0L);
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(
                promotionService.getUserNotifications(userId, PageRequest.of(page, size)))));
    }

    @GetMapping("/api/v1/notifications/unread-count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(Authentication auth) {
        Long userId = userRepository.findByEmail(auth.getName())
                .or(() -> userRepository.findByMobile(auth.getName()))
                .map(u -> u.getId()).orElse(0L);
        return ResponseEntity.ok(ApiResponse.ok(promotionService.getUnreadCount(userId)));
    }

    @PatchMapping("/api/v1/notifications/mark-all-read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<String>> markAllRead(Authentication auth) {
        Long userId = userRepository.findByEmail(auth.getName())
                .or(() -> userRepository.findByMobile(auth.getName()))
                .map(u -> u.getId()).orElse(0L);
        promotionService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.ok("All notifications marked as read"));
    }
}
