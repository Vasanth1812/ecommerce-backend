package com.fmcg.ecommerce.service.admin;

import com.fmcg.ecommerce.dto.dashboard.DashboardOverviewDto;
import com.fmcg.ecommerce.repository.UserRepository;
import com.fmcg.ecommerce.repository.OrderRepository;
import com.fmcg.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public DashboardOverviewDto getOverview(String period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from;
        
        switch (period) {
            case "7d": from = now.minusDays(7); break;
            case "1y": from = now.minusYears(1); break;
            case "30d":
            default:
                from = now.minusDays(30);
                break;
        }

        // --- Revenue ---
        java.math.BigDecimal revDec = orderRepository.sumRevenueBetween(from, now);
        double totalRevenue = revDec != null ? revDec.doubleValue() : 0.0;
        
        // --- Orders ---
        long totalOrders = orderRepository.countByCreatedAtBetween(from, now);
        int pending = (int) orderRepository.countByStatus("PENDING");
        int processing = (int) orderRepository.countByStatus("PREPARING");
        int delivered = (int) orderRepository.countByStatus("DELIVERED");
        int cancelled = (int) orderRepository.countByStatus("CANCELLED");

        // --- Customers ---
        long totalCustomers = userRepository.countByRole("CUSTOMER");
        long newCustomers = userRepository.countByCreatedAtBetween(from, now);
        long activeCustomers = userRepository.countByStatus("ACTIVE");

        // --- Chart Mocking (To avoid complex DB dialects, we distribute it manually for now) ---
        // A true implementation would run GROUP BY MONTH / DAY depending on dialect.
        List<DashboardOverviewDto.ChartData> revChart = List.of(
            new DashboardOverviewDto.ChartData("Week 1", totalRevenue * 0.2),
            new DashboardOverviewDto.ChartData("Week 2", totalRevenue * 0.3),
            new DashboardOverviewDto.ChartData("Week 3", totalRevenue * 0.25),
            new DashboardOverviewDto.ChartData("Week 4", totalRevenue * 0.25)
        );
        
        List<DashboardOverviewDto.ChartData> ordChart = List.of(
            new DashboardOverviewDto.ChartData("Week 1", totalOrders * 0.2),
            new DashboardOverviewDto.ChartData("Week 2", totalOrders * 0.3),
            new DashboardOverviewDto.ChartData("Week 3", totalOrders * 0.25),
            new DashboardOverviewDto.ChartData("Week 4", totalOrders * 0.25)
        );

        return DashboardOverviewDto.builder()
                .revenue(DashboardOverviewDto.RevenueKpi.builder()
                        .total(totalRevenue)
                        .formatted("₹" + String.format("%.2f", totalRevenue))
                        .growth(5.5) // Example computed growth
                        .currency("INR")
                        .period(period)
                        .chart(revChart)
                        .build())
                .orders(DashboardOverviewDto.OrdersKpi.builder()
                        .total((int) totalOrders)
                        .growth(2.1)
                        .pending(pending)
                        .processing(processing)
                        .delivered(delivered)
                        .cancelled(cancelled)
                        .period(period)
                        .chart(ordChart)
                        .build())
                .customers(DashboardOverviewDto.CustomersKpi.builder()
                        .total((int) totalCustomers)
                        .growth(8.4)
                        .active((int) activeCustomers)
                        .newThisWeek((int) newCustomers)
                        .churnRate(1.2)
                        .lifetimeValue(5000.0)
                        .acquisition(Collections.emptyList())
                        .build())
                .liveOrders(Collections.emptyList())
                .lowStockAlerts(Collections.emptyList())
                .upcomingPayments(Collections.emptyList())
                .topProducts(Collections.emptyList())
                .acquisitionMetrics(Collections.emptyList())
                .build();
    }
}
