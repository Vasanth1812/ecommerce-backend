package com.fmcg.ecommerce.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

public class ReportDTO {

    // --- GST Reports ---
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class GSTReportEntry {
        private String id;
        private String period;
        private String gstin;
        private String businessName;
        private BigDecimal grossSales;
        private BigDecimal taxableValue;
        private BigDecimal cgst;
        private BigDecimal sgst;
        private BigDecimal igst;
        private BigDecimal totalTaxLiability;
        private BigDecimal inputCredit;
        private BigDecimal netPayable;
        private String returnType; // GSTR-1, GSTR-3B, GSTR-9
        private String status; // filed, pending, overdue, processing
        private String dueDate;
        private String filedDate;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class GSTSummary {
        private BigDecimal totalLiability;
        private BigDecimal totalInputCredit;
        private BigDecimal netPayable;
        private Integer pendingReturns;
        private Integer overdueReturns;
    }

    // --- Customer Reports ---
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CustomerReportEntry {
        private String customerId;
        private String name;
        private String email;
        private Integer totalOrders;
        private BigDecimal totalSpent;
        private BigDecimal avgOrderValue;
        private String lastOrderDate;
        private String lifetimeValue;
        private String segment; // platinum, gold, silver, bronze, new
        private String acquisitionChannel;
        private Integer retentionRate;
        private Integer ordersThisMonth;
        private String preferredCategory;
        private String city;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CustomerSummary {
        private Integer totalCustomers;
        private BigDecimal totalRevenue;
        private Integer avgRetentionRate;
        private Integer platinumCount;
        private Integer atRiskCount;
    }

    // --- Cohort Reports ---
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CohortEntry {
        private String id;
        private String cohort;
        private String period;
        private Integer users;
        private Integer week0;
        private Integer week1;
        private Integer week2;
        private Integer week3;
        private Integer week4;
        private Integer week5;
        private Integer week6;
        private Integer week7;
        private Integer week8;
        private Integer week9;
        private Integer week10;
        private Integer week11;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CohortSummary {
        private Integer totalCohorts;
        private Integer totalUsers;
        private BigDecimal avgRetentionWeek1;
        private BigDecimal avgRetentionWeek4;
        private BigDecimal avgRetentionWeek12;
        private String bestPerformingCohort;
        private String worstPerformingCohort;
    }

    // --- Abandoned Cart Reports ---
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AbandonedCartEntry {
        private String id;
        private String customerName;
        private String customerEmail;
        private Integer items;
        private BigDecimal cartValue;
        private String status; // abandoned, recovered, lost
        private String abandonedAt;
        private String recoveredAt;
        private String recoveryMethod;
        private List<String> itemsList;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AbandonedCartSummary {
        private Integer totalAbandoned;
        private Integer totalRecovered;
        private BigDecimal recoveryRate;
        private BigDecimal lostRevenue;
        private BigDecimal recoveredRevenue;
        private BigDecimal avgCartValue;
        private String topAbandonedCategory;
    }

    // --- Revenue Analytics ---
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RevenueAnalyticsEntry {
        private String id;
        private String month;
        private BigDecimal revenue;
        private BigDecimal cogs;
        private BigDecimal grossProfit;
        private BigDecimal grossMargin;
        private BigDecimal operatingExpenses;
        private BigDecimal operatingProfit;
        private BigDecimal netProfit;
        private BigDecimal ebitda;
        private BigDecimal revenuePerOrder;
        private BigDecimal costPerOrder;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RevenueSummary {
        private BigDecimal totalRevenue;
        private BigDecimal totalCOGS;
        private BigDecimal totalGrossProfit;
        private BigDecimal avgGrossMargin;
        private BigDecimal totalNetProfit;
        private BigDecimal revenueGrowth;
        private BigDecimal profitGrowth;
    }

    // --- Promotion ROI Reports ---
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PromotionROIEntry {
        private String id;
        private String promotionName;
        private String type; // percentage, fixed, bogo, free_shipping
        private BigDecimal cost;
        private BigDecimal revenueGenerated;
        private Integer ordersIncremented;
        private Integer redemptionCount;
        private BigDecimal roi;
        private BigDecimal conversionRate;
        private BigDecimal avgOrderValue;
        private String status; // active, completed, scheduled, ended
        private String startDate;
        private String endDate;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PromotionROISummary {
        private Integer totalPromotions;
        private BigDecimal totalCost;
        private BigDecimal totalRevenue;
        private BigDecimal avgROI;
        private String bestPromotion;
        private BigDecimal highestROI;
        private Integer totalRedemptions;
    }

    // --- Vendor Reports ---
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class VendorReportEntry {
        private String id;
        private String vendorId;
        private String vendorName;
        private String category;
        private Integer totalOrders;
        private BigDecimal grossSales;
        private BigDecimal commission;
        private BigDecimal commissionRate;
        private BigDecimal netPayout;
        private BigDecimal pendingPayout;
        private BigDecimal rating;
        private String performance; // excellent, good, average, poor
        private BigDecimal onTimeDeliveryRate;
        private BigDecimal returnRate;
        private Integer activeProducts;
        private String joinedDate;
        private String lastPayoutDate;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class VendorReportSummary {
        private Integer totalVendors;
        private BigDecimal totalGrossSales;
        private BigDecimal totalCommission;
        private BigDecimal totalNetPayout;
        private BigDecimal totalPendingPayout;
        private BigDecimal avgRating;
        private Integer excellentCount;
        private Integer poorCount;
    }

    // --- Tax Reports ---
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TaxReportEntry {
        private String id;
        private String reportTitle;
        private String period;
        private String type; // GSTR-1, GSTR-3B, GSTR-9, TDS, ITC, Annual
        private String generatedBy;
        private String status; // generated, filed, archived, error
        private String dueDate;
        private String filedDate;
        private BigDecimal totalTax;
        private String downloadUrl;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TaxReportSummary {
        private Integer generatedReports;
        private Integer pendingFilings;
        private Integer completedFilings;
        private BigDecimal upcomingTaxLiability;
        private String nextDeadline;
        private String nextDeadlineType;
    }
}
