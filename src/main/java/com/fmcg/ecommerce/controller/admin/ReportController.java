package com.fmcg.ecommerce.controller.admin;

import com.fmcg.ecommerce.common.ApiResponse;
import com.fmcg.ecommerce.entity.LoyaltyAccount;
import com.fmcg.ecommerce.entity.LoyaltyTransaction;
import com.fmcg.ecommerce.entity.Order;
import com.fmcg.ecommerce.repository.LoyaltyAccountRepository;
import com.fmcg.ecommerce.repository.LoyaltyTransactionRepository;
import com.fmcg.ecommerce.repository.OrderRepository;
import com.fmcg.ecommerce.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reports & Loyalty", description = "Sales reports and loyalty program")
public class ReportController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;

    // ── Reports ───────────────────────────────────────────

    @GetMapping("/api/v1/admin/reports/sales")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Sales report by date range")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSalesReport(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        LocalDateTime fromDt = from != null ? from.atStartOfDay() : LocalDateTime.now().minusDays(30);
        LocalDateTime toDt = to != null ? to.atTime(23, 59, 59) : LocalDateTime.now();

        BigDecimal revenue = orderRepository.sumRevenueBetween(fromDt, toDt);
        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.countByStatus("PENDING");
        long deliveredOrders = orderRepository.countByStatus("DELIVERED");
        long cancelledOrders = orderRepository.countByStatus("CANCELLED");

        Map<String, Object> report = new HashMap<>();
        report.put("from", fromDt);
        report.put("to", toDt);
        report.put("totalRevenue", revenue != null ? revenue : BigDecimal.ZERO);
        report.put("totalOrders", totalOrders);
        report.put("deliveredOrders", deliveredOrders);
        report.put("pendingOrders", pendingOrders);
        report.put("cancelledOrders", cancelledOrders);
        report.put("averageOrderValue", totalOrders > 0 && revenue != null
                ? revenue.divide(BigDecimal.valueOf(totalOrders), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        return ResponseEntity.ok(ApiResponse.ok(report));
    }

    @GetMapping("/api/v1/admin/reports/sales/export")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Export sales report as CSV")
    public ResponseEntity<byte[]> exportSalesReportCsv(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {

        LocalDateTime fromDt = from != null ? from.atStartOfDay() : LocalDateTime.now().minusDays(30);
        LocalDateTime toDt = to != null ? to.atTime(23, 59, 59) : LocalDateTime.now();

        // Fetch paginated orders in the date range (up to 10,000 rows for export)
        List<Order> orders = orderRepository.adminSearchOrders(
                null, fromDt, toDt, null,
                PageRequest.of(0, 10000)
        ).getContent();

        StringBuilder csv = new StringBuilder();
        // Header
        csv.append("Order Number,Customer Name,Customer Email,Status,Payment Method,Payment Status,")
           .append("Subtotal,Discount,Delivery Fee,Tax,Total,Items Count,Created At\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Order order : orders) {
            int itemCount = order.getItems() != null ? order.getItems().size() : 0;
            csv.append(escape(order.getOrderNumber())).append(",")
               .append(escape(order.getUser().getName())).append(",")
               .append(escape(order.getUser().getEmail() != null ? order.getUser().getEmail() : "")).append(",")
               .append(escape(order.getStatus())).append(",")
               .append(escape(order.getPaymentMethod())).append(",")
               .append(escape(order.getPaymentStatus())).append(",")
               .append(order.getSubtotal()).append(",")
               .append(order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO).append(",")
               .append(order.getDeliveryFee() != null ? order.getDeliveryFee() : BigDecimal.ZERO).append(",")
               .append(order.getTaxAmount() != null ? order.getTaxAmount() : BigDecimal.ZERO).append(",")
               .append(order.getTotal()).append(",")
               .append(itemCount).append(",")
               .append(order.getCreatedAt() != null ? order.getCreatedAt().format(formatter) : "").append("\n");
        }

        // Summary footer
        BigDecimal totalRevenue = orders.stream()
                .filter(o -> "PAID".equals(o.getPaymentStatus()))
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        csv.append("\n");
        csv.append(",,,,,,,,,,SUMMARY\n");
        csv.append("Total Orders,").append(orders.size()).append("\n");
        csv.append("Total Revenue (Paid),").append(totalRevenue).append("\n");
        csv.append("Average Order Value,").append(
                orders.isEmpty() ? "0" :
                totalRevenue.divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP)
        ).append("\n");
        csv.append("Report Period,").append(fromDt.format(formatter)).append(" to ").append(toDt.format(formatter)).append("\n");
        csv.append("Generated At,").append(LocalDateTime.now().format(formatter)).append("\n");

        String filename = "sales_report_" + (from != null ? from : "last30days") + "_to_" + (to != null ? to : "today") + ".csv";
        byte[] csvBytes = csv.toString().getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .contentLength(csvBytes.length)
                .body(csvBytes);
    }

    @GetMapping("/api/v1/admin/reports/inventory")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Inventory summary report")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInventoryReport() {
        Map<String, Object> report = new HashMap<>();
        report.put("generatedAt", LocalDateTime.now());
        report.put("note", "Use /api/v1/admin/inventory/report for full inventory stats");
        return ResponseEntity.ok(ApiResponse.ok(report));
    }

    // ── Loyalty ───────────────────────────────────────────

    @GetMapping("/api/v1/loyalty/balance")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my loyalty points and tier")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLoyaltyBalance(Authentication auth) {
        Long userId = userRepository.findByEmail(auth.getName())
                .or(() -> userRepository.findByMobile(auth.getName()))
                .map(u -> u.getId()).orElse(0L);

        Optional<LoyaltyAccount> accountOpt = loyaltyAccountRepository.findByUserId(userId);
        Map<String, Object> balance = new HashMap<>();
        if (accountOpt.isPresent()) {
            LoyaltyAccount la = accountOpt.get();
            balance.put("points", la.getPointsBalance());
            balance.put("tier", la.getTier());
            int nextTierPoints = "SILVER".equals(la.getTier()) ? 1000
                    : "GOLD".equals(la.getTier()) ? 5000 : -1;
            balance.put("nextTierPoints", nextTierPoints);
            balance.put("nextTier", "SILVER".equals(la.getTier()) ? "GOLD"
                    : "GOLD".equals(la.getTier()) ? "PLATINUM" : "MAX");
            balance.put("pointsToNext", nextTierPoints > 0
                    ? Math.max(0, nextTierPoints - la.getPointsBalance()) : 0);
        } else {
            balance.put("points", 0);
            balance.put("tier", "SILVER");
            balance.put("nextTierPoints", 1000);
            balance.put("nextTier", "GOLD");
        }
        return ResponseEntity.ok(ApiResponse.ok(balance));
    }

    @GetMapping("/api/v1/loyalty/transactions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my loyalty transaction history")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLoyaltyTransactions(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = userRepository.findByEmail(auth.getName())
                .or(() -> userRepository.findByMobile(auth.getName()))
                .map(u -> u.getId()).orElse(0L);
        var transactions = loyaltyTransactionRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "content", transactions.getContent(),
                "totalElements", transactions.getTotalElements(),
                "totalPages", transactions.getTotalPages()
        )));
    }

    // ── CSV Helper ────────────────────────────────────────

    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
