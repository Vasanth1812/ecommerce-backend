package com.fmcg.ecommerce.controller.admin;

import com.fmcg.ecommerce.common.ApiResponse;
import com.fmcg.ecommerce.common.PagedResponse;
import com.fmcg.ecommerce.entity.Coupon;
import com.fmcg.ecommerce.entity.Notification;
import com.fmcg.ecommerce.entity.Promotion;
import com.fmcg.ecommerce.entity.MarketingCampaign;
import com.fmcg.ecommerce.entity.AbTest;
import com.fmcg.ecommerce.repository.UserRepository;
import com.fmcg.ecommerce.service.impl.PromotionServiceImpl;
import com.fmcg.ecommerce.service.admin.AdminPromotionService;
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
    private final AdminPromotionService adminPromotionService;
    private final UserRepository userRepository;

    // â”€â”€ Promotions â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

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

    // â”€â”€ Coupons â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

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

    // --- Flash Sales ---
    @GetMapping("/api/v1/admin/promotions/flash-sales")
    @Operation(summary = "List all flash sales")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFlashSales() {
        List<Promotion> sales = adminPromotionService.getFlashSales();
        Map<String, Object> summary = new java.util.HashMap<>();
        summary.put("live", sales.stream().filter(s -> "ACTIVE".equals(s.getStatus())).count());
        summary.put("scheduled", sales.stream().filter(s -> "SCHEDULED".equals(s.getStatus())).count());
        summary.put("completed", sales.stream().filter(s -> "EXPIRED".equals(s.getStatus())).count());
        summary.put("totalBudget", "$15,000"); // Mock budget

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("flashSales", sales);
        response.put("summary", summary);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/api/v1/admin/promotions/flash-sales")
    @Operation(summary = "Create a flash sale")
    public ResponseEntity<ApiResponse<Promotion>> createFlashSale(@RequestBody Promotion promotion) {
        return ResponseEntity.ok(ApiResponse.ok(adminPromotionService.createFlashSale(promotion)));
    }

    @PutMapping("/api/v1/admin/promotions/flash-sales/{id}")
    @Operation(summary = "Update a flash sale")
    public ResponseEntity<ApiResponse<Promotion>> updateFlashSale(
            @PathVariable Long id, @RequestBody Promotion promotion) {
        return ResponseEntity.ok(ApiResponse.ok(adminPromotionService.updateFlashSale(id, promotion)));
    }

    // --- Campaigns ---
    @GetMapping("/api/v1/admin/promotions/campaigns")
    @Operation(summary = "List all marketing campaigns")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCampaigns() {
        List<MarketingCampaign> campaigns = adminPromotionService.getCampaigns();
        Map<String, Object> summary = new java.util.HashMap<>();
        summary.put("active", campaigns.stream().filter(c -> "ACTIVE".equals(c.getStatus())).count());
        summary.put("scheduled", campaigns.stream().filter(c -> "SCHEDULED".equals(c.getStatus())).count());
        summary.put("drafts", campaigns.stream().filter(c -> "DRAFT".equals(c.getStatus())).count());
        summary.put("totalReach", "125,400"); // Mock reach

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("campaigns", campaigns);
        response.put("summary", summary);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/api/v1/admin/promotions/campaigns")
    @Operation(summary = "Create a new campaign")
    public ResponseEntity<ApiResponse<MarketingCampaign>> createCampaign(@RequestBody MarketingCampaign campaign) {
        return ResponseEntity.ok(ApiResponse.ok(adminPromotionService.createCampaign(campaign)));
    }

    @PutMapping("/api/v1/admin/promotions/campaigns/{id}")
    @Operation(summary = "Update a campaign")
    public ResponseEntity<ApiResponse<MarketingCampaign>> updateCampaign(
            @PathVariable Long id, @RequestBody MarketingCampaign campaign) {
        return ResponseEntity.ok(ApiResponse.ok(adminPromotionService.updateCampaign(id, campaign)));
    }

    // --- Push Notifications ---
    @PostMapping("/api/v1/admin/promotions/push")
    @Operation(summary = "Trigger a push notification blast")
    public ResponseEntity<ApiResponse<Notification>> sendPushNotification(@RequestBody Map<String, Object> payload) {
        Long userId = payload.get("userId") != null ? Long.valueOf(payload.get("userId").toString()) : 1L; // Fallback to 1 for test
        String title = payload.getOrDefault("title", "").toString();
        String message = payload.getOrDefault("message", "").toString();
        return ResponseEntity.ok(ApiResponse.ok(adminPromotionService.sendPushNotification(userId, title, message)));
    }
    
    @GetMapping("/api/v1/admin/promotions/push/summary")
    @Operation(summary = "Get push notification summary metrics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPushSummary() {
        Map<String, Object> summary = new java.util.HashMap<>();
        summary.put("sent", 12450);
        summary.put("scheduled", 3);
        summary.put("drafts", 8);
        summary.put("avgOpenRate", "24.5%");
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    // --- A/B Tests ---
    @GetMapping("/api/v1/admin/promotions/ab-tests")
    @Operation(summary = "List all A/B tests")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAbTests() {
        List<AbTest> tests = adminPromotionService.getAbTests();
        Map<String, Object> summary = new java.util.HashMap<>();
        summary.put("total", tests.size());
        summary.put("running", tests.stream().filter(t -> "RUNNING".equals(t.getStatus())).count());
        summary.put("completed", tests.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count());
        int totalImpressions = tests.stream().mapToInt(t -> (t.getImpressionsA() != null ? t.getImpressionsA() : 0) + (t.getImpressionsB() != null ? t.getImpressionsB() : 0)).sum();
        summary.put("totalImpressions", totalImpressions);

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("tests", tests);
        response.put("summary", summary);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/api/v1/admin/promotions/ab-tests")
    @Operation(summary = "Create an A/B test")
    public ResponseEntity<ApiResponse<AbTest>> createAbTest(@RequestBody AbTest test) {
        return ResponseEntity.ok(ApiResponse.ok(adminPromotionService.createAbTest(test)));
    }
}
