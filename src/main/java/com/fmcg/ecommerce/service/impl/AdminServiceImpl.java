package com.fmcg.ecommerce.service.impl;

import com.fmcg.ecommerce.dto.auth.UserSummaryDto;
import com.fmcg.ecommerce.entity.CustomerNote;
import com.fmcg.ecommerce.entity.Inventory;
import com.fmcg.ecommerce.entity.Order;
import com.fmcg.ecommerce.entity.User;
import com.fmcg.ecommerce.exception.BadRequestException;
import com.fmcg.ecommerce.exception.ResourceNotFoundException;
import com.fmcg.ecommerce.repository.CustomerNoteRepository;
import com.fmcg.ecommerce.repository.InventoryRepository;
import com.fmcg.ecommerce.repository.OrderRepository;
import com.fmcg.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final CustomerNoteRepository customerNoteRepository;

    // ── Dashboard ─────────────────────────────────────────

    public Map<String, Object> getDashboardOverview(String period) {
        int days = switch (period) {
            case "7d" -> 7;
            case "90d" -> 90;
            default -> 30;
        };
        LocalDateTime from = LocalDateTime.now().minusDays(days);
        LocalDateTime to = LocalDateTime.now();

        BigDecimal revenue = orderRepository.sumRevenueBetween(from, to);
        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.countByStatus("PENDING");
        long deliveredOrders = orderRepository.countByStatus("DELIVERED");
        long cancelledOrders = orderRepository.countByStatus("CANCELLED");
        long totalCustomers = userRepository.countByRole("CUSTOMER");
        List<Inventory> lowStock = inventoryRepository.findLowStockItems();
        List<Order> recentOrders = orderRepository.findTop10ByOrderByCreatedAtDesc();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("period", period);
        dashboard.put("revenue", revenue != null ? revenue : BigDecimal.ZERO);
        dashboard.put("totalOrders", totalOrders);
        dashboard.put("ordersByStatus", Map.of(
                "PENDING", pendingOrders,
                "DELIVERED", deliveredOrders,
                "CANCELLED", cancelledOrders
        ));
        dashboard.put("totalCustomers", totalCustomers);
        dashboard.put("lowStockAlerts", lowStock.size());
        dashboard.put("recentOrders", recentOrders.stream().map(o -> Map.of(
                "id", o.getId(),
                "orderNumber", o.getOrderNumber(),
                "customerName", o.getUser().getName(),
                "status", o.getStatus(),
                "total", o.getTotal(),
                "createdAt", o.getCreatedAt()
        )).collect(Collectors.toList()));
        return dashboard;
    }

    // ── Customers ─────────────────────────────────────────

    public Page<Map<String, Object>> getCustomers(String search, String status, Pageable pageable) {
        return userRepository.searchCustomers(search, pageable).map(u -> {
            long totalOrders = orderRepository.countByUserId(u.getId());
            BigDecimal totalSpent = orderRepository.sumTotalSpentByUser(u.getId());
            String segment = totalSpent != null && totalSpent.compareTo(new BigDecimal("50000")) >= 0 ? "VIP"
                    : totalOrders >= 5 ? "REGULAR" : "NEW";

            return Map.<String, Object>of(
                    "id", u.getId(),
                    "name", u.getName(),
                    "email", u.getEmail() != null ? u.getEmail() : "",
                    "mobile", u.getMobile() != null ? u.getMobile() : "",
                    "status", u.getStatus(),
                    "segment", segment,
                    "totalOrders", totalOrders,
                    "totalSpent", totalSpent != null ? totalSpent : BigDecimal.ZERO,
                    "createdAt", u.getCreatedAt()
            );
        });
    }

    public Map<String, Object> getCustomerById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        long totalOrders = orderRepository.countByUserId(id);
        BigDecimal totalSpent = orderRepository.sumTotalSpentByUser(id);
        return Map.of(
                "id", user.getId(), "name", user.getName(),
                "email", user.getEmail() != null ? user.getEmail() : "",
                "mobile", user.getMobile() != null ? user.getMobile() : "",
                "status", user.getStatus(), "role", user.getRole(),
                "totalOrders", totalOrders,
                "totalSpent", totalSpent != null ? totalSpent : BigDecimal.ZERO,
                "createdAt", user.getCreatedAt()
        );
    }

    public UserSummaryDto updateCustomerStatus(Long id, String status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        user.setStatus(status);
        user = userRepository.save(user);
        return UserSummaryDto.builder().id(user.getId()).name(user.getName())
                .email(user.getEmail()).mobile(user.getMobile())
                .role(user.getRole()).status(user.getStatus()).build();
    }

    public Map<String, Object> getCustomerStats() {
        long total = userRepository.countByRole("CUSTOMER");
        long active = userRepository.countByStatus("ACTIVE");
        long blocked = userRepository.countByStatus("BLOCKED");
        return Map.of("total", total, "active", active, "blocked", blocked);
    }

    // ── Customer Notes ────────────────────────────────────

    public List<CustomerNote> getCustomerNotes(Long customerId) {
        if (!userRepository.existsById(customerId))
            throw new ResourceNotFoundException("Customer", customerId);
        return customerNoteRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public CustomerNote addCustomerNote(Long customerId, String note, String adminEmail) {
        if (note == null || note.isBlank())
            throw new BadRequestException("Note cannot be empty");
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));
        CustomerNote customerNote = CustomerNote.builder()
                .customer(customer)
                .note(note.trim())
                .createdBy(adminEmail)
                .build();
        return customerNoteRepository.save(customerNote);
    }

    public void deleteCustomerNote(Long noteId, String adminEmail) {
        CustomerNote note = customerNoteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note", noteId));
        customerNoteRepository.delete(note);
    }
}
