package com.fmcg.ecommerce.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewDto {
    private RevenueKpi revenue;
    private OrdersKpi orders;
    private CustomersKpi customers;
    private List<LiveOrder> liveOrders;
    private List<StockAlert> lowStockAlerts;
    private List<VendorPayment> upcomingPayments;
    private List<TopProduct> topProducts;
    private List<AcquisitionMetric> acquisitionMetrics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueKpi {
        private double total;
        private String formatted;
        private double growth;
        private String currency;
        private String period;
        private List<ChartData> chart;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrdersKpi {
        private int total;
        private double growth;
        private int pending;
        private int processing;
        private int delivered;
        private int cancelled;
        private String period;
        private List<ChartData> chart;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomersKpi {
        private int total;
        private double growth;
        private int active;
        private int newThisWeek;
        private double churnRate;
        private double lifetimeValue;
        private List<AcquisitionMetric> acquisition;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartData {
        private String label;
        private double value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LiveOrder {
        private String id;
        private String customer;
        private int items;
        private double total;
        private String status;
        private String time;
        private String area;
        private String assignedPartner;
        private String estimatedDelivery;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockAlert {
        private String id;
        private String name;
        private String sku;
        private int stock;
        private int threshold;
        private String warehouse;
        private String category;
        private String status;
        private String lastRestocked;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VendorPayment {
        private String id;
        private String vendorName;
        private double amount;
        private String formattedAmount;
        private String dueDate;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProduct {
        private String id;
        private String name;
        private String category;
        private int unitsSold;
        private double revenue;
        private String formattedRevenue;
        private String trend;
        private String image;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AcquisitionMetric {
        private String source;
        private int count;
        private int percentage;
        private String trend;
        private String color;
    }
}
