package com.fmcg.ecommerce.service.impl;

import com.fmcg.ecommerce.dto.report.ReportDTO.*;
import com.fmcg.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;

    // --- Vendors ---
    @Transactional(readOnly = true)
    public Page<VendorReportEntry> getVendorReports(String search, Pageable pageable) {
        List<VendorReportEntry> list = new ArrayList<>();
        list.add(VendorReportEntry.builder()
                .id("1").vendorId("V-001").vendorName("Fresh Farms Ltd").category("Produce")
                .totalOrders(145).grossSales(new BigDecimal("125000")).commission(new BigDecimal("12500"))
                .commissionRate(new BigDecimal("10.0")).netPayout(new BigDecimal("112500")).pendingPayout(new BigDecimal("0"))
                .rating(new BigDecimal("4.8")).performance("excellent").onTimeDeliveryRate(new BigDecimal("98.5"))
                .returnRate(new BigDecimal("1.2")).activeProducts(45).joinedDate("2023-01-15").lastPayoutDate("2024-05-01")
                .build());
        return new PageImpl<>(list, pageable, list.size());
    }

    @Transactional(readOnly = true)
    public VendorReportSummary getVendorSummary() {
        return VendorReportSummary.builder()
                .totalVendors((int) vendorRepository.count())
                .totalGrossSales(new BigDecimal("1250000"))
                .totalCommission(new BigDecimal("125000"))
                .totalNetPayout(new BigDecimal("1125000"))
                .totalPendingPayout(new BigDecimal("45000"))
                .avgRating(new BigDecimal("4.5"))
                .excellentCount(12)
                .poorCount(2)
                .build();
    }

    // --- Taxes ---
    @Transactional(readOnly = true)
    public Page<TaxReportEntry> getTaxReports(String search, Pageable pageable) {
        List<TaxReportEntry> list = new ArrayList<>();
        list.add(TaxReportEntry.builder()
                .id("1").reportTitle("Q1 2024 GST Return").period("Q1 2024").type("GSTR-3B")
                .generatedBy("System").status("filed").dueDate("2024-04-20").filedDate("2024-04-18")
                .totalTax(new BigDecimal("45600.50")).downloadUrl("/api/v1/downloads/tax/1")
                .build());
        return new PageImpl<>(list, pageable, list.size());
    }

    @Transactional(readOnly = true)
    public TaxReportSummary getTaxSummary() {
        return TaxReportSummary.builder()
                .generatedReports(24)
                .pendingFilings(3)
                .completedFilings(21)
                .upcomingTaxLiability(new BigDecimal("12500.00"))
                .nextDeadline("2024-06-20")
                .nextDeadlineType("GSTR-3B")
                .build();
    }

    // --- Customers ---
    @Transactional(readOnly = true)
    public Page<CustomerReportEntry> getCustomerReports(String search, Pageable pageable) {
        List<CustomerReportEntry> list = new ArrayList<>();
        list.add(CustomerReportEntry.builder()
                .customerId("C-001").name("John Doe").email("john.doe@example.com")
                .totalOrders(12).totalSpent(new BigDecimal("4500")).avgOrderValue(new BigDecimal("375"))
                .lastOrderDate("2024-05-28").lifetimeValue("High").segment("platinum")
                .acquisitionChannel("Organic").retentionRate(85).ordersThisMonth(2)
                .preferredCategory("Groceries").city("Mumbai")
                .build());
        return new PageImpl<>(list, pageable, list.size());
    }

    @Transactional(readOnly = true)
    public CustomerSummary getCustomerSummary() {
        return CustomerSummary.builder()
                .totalCustomers((int) userRepository.count())
                .totalRevenue(new BigDecimal("540000"))
                .avgRetentionRate(76)
                .platinumCount(150)
                .atRiskCount(45)
                .build();
    }

    // --- GST ---
    @Transactional(readOnly = true)
    public Page<GSTReportEntry> getGSTReports(String search, Pageable pageable) {
        List<GSTReportEntry> list = new ArrayList<>();
        list.add(GSTReportEntry.builder()
                .id("1").period("May 2024").gstin("27AABCB1234D1Z5").businessName("FMCG Corp")
                .grossSales(new BigDecimal("250000")).taxableValue(new BigDecimal("225000"))
                .cgst(new BigDecimal("11250")).sgst(new BigDecimal("11250")).igst(new BigDecimal("0"))
                .totalTaxLiability(new BigDecimal("22500")).inputCredit(new BigDecimal("5000"))
                .netPayable(new BigDecimal("17500")).returnType("GSTR-3B").status("pending")
                .dueDate("2024-06-20").build());
        return new PageImpl<>(list, pageable, list.size());
    }

    @Transactional(readOnly = true)
    public GSTSummary getGSTSummary() {
        return GSTSummary.builder()
                .totalLiability(new BigDecimal("225000"))
                .totalInputCredit(new BigDecimal("45000"))
                .netPayable(new BigDecimal("180000"))
                .pendingReturns(2)
                .overdueReturns(0)
                .build();
    }

    // --- Cohorts ---
    @Transactional(readOnly = true)
    public Page<CohortEntry> getCohortData(Pageable pageable) {
        List<CohortEntry> list = new ArrayList<>();
        list.add(CohortEntry.builder()
                .id("1").cohort("Jan 2024").period("Monthly").users(1200)
                .week0(100).week1(45).week2(35).week3(30).week4(28)
                .week5(25).week6(24).week7(22).week8(20).week9(18)
                .week10(15).week11(12).build());
        return new PageImpl<>(list, pageable, list.size());
    }

    @Transactional(readOnly = true)
    public CohortSummary getCohortSummary() {
        return CohortSummary.builder()
                .totalCohorts(12)
                .totalUsers(15000)
                .avgRetentionWeek1(new BigDecimal("42.5"))
                .avgRetentionWeek4(new BigDecimal("28.3"))
                .avgRetentionWeek12(new BigDecimal("15.1"))
                .bestPerformingCohort("Nov 2023")
                .worstPerformingCohort("Feb 2024")
                .build();
    }

    // --- Abandoned Carts ---
    @Transactional(readOnly = true)
    public Page<AbandonedCartEntry> getAbandonedCartData(String search, Pageable pageable) {
        List<AbandonedCartEntry> list = new ArrayList<>();
        list.add(AbandonedCartEntry.builder()
                .id("1").customerName("Alice Smith").customerEmail("alice@example.com")
                .items(3).cartValue(new BigDecimal("1250")).status("abandoned")
                .abandonedAt("2024-06-01T14:30:00").itemsList(List.of("Organic Honey", "Almonds"))
                .build());
        return new PageImpl<>(list, pageable, list.size());
    }

    @Transactional(readOnly = true)
    public AbandonedCartSummary getAbandonedCartSummary() {
        return AbandonedCartSummary.builder()
                .totalAbandoned(450)
                .totalRecovered(120)
                .recoveryRate(new BigDecimal("26.6"))
                .lostRevenue(new BigDecimal("150000"))
                .recoveredRevenue(new BigDecimal("45000"))
                .avgCartValue(new BigDecimal("850"))
                .topAbandonedCategory("Snacks")
                .build();
    }

    // --- Revenue Analytics ---
    @Transactional(readOnly = true)
    public Page<RevenueAnalyticsEntry> getRevenueAnalytics(Pageable pageable) {
        List<RevenueAnalyticsEntry> list = new ArrayList<>();
        list.add(RevenueAnalyticsEntry.builder()
                .id("1").month("May 2024").revenue(new BigDecimal("500000"))
                .cogs(new BigDecimal("300000")).grossProfit(new BigDecimal("200000"))
                .grossMargin(new BigDecimal("40.0")).operatingExpenses(new BigDecimal("50000"))
                .operatingProfit(new BigDecimal("150000")).netProfit(new BigDecimal("120000"))
                .ebitda(new BigDecimal("160000")).revenuePerOrder(new BigDecimal("1250"))
                .costPerOrder(new BigDecimal("750")).build());
        return new PageImpl<>(list, pageable, list.size());
    }

    @Transactional(readOnly = true)
    public RevenueSummary getRevenueSummary() {
        return RevenueSummary.builder()
                .totalRevenue(new BigDecimal("5000000"))
                .totalCOGS(new BigDecimal("3000000"))
                .totalGrossProfit(new BigDecimal("2000000"))
                .avgGrossMargin(new BigDecimal("40.0"))
                .totalNetProfit(new BigDecimal("1200000"))
                .revenueGrowth(new BigDecimal("12.5"))
                .profitGrowth(new BigDecimal("15.2"))
                .build();
    }

    // --- Promotion ROI ---
    @Transactional(readOnly = true)
    public Page<PromotionROIEntry> getPromotionReports(String search, Pageable pageable) {
        List<PromotionROIEntry> list = new ArrayList<>();
        list.add(PromotionROIEntry.builder()
                .id("1").promotionName("Summer Sale 2024").type("percentage")
                .cost(new BigDecimal("15000")).revenueGenerated(new BigDecimal("150000"))
                .ordersIncremented(300).redemptionCount(450).roi(new BigDecimal("900.0"))
                .conversionRate(new BigDecimal("5.5")).avgOrderValue(new BigDecimal("500"))
                .status("active").startDate("2024-05-01").endDate("2024-05-31")
                .build());
        return new PageImpl<>(list, pageable, list.size());
    }

    @Transactional(readOnly = true)
    public PromotionROISummary getPromotionSummary() {
        return PromotionROISummary.builder()
                .totalPromotions(24)
                .totalCost(new BigDecimal("120000"))
                .totalRevenue(new BigDecimal("1500000"))
                .avgROI(new BigDecimal("1150.0"))
                .bestPromotion("Diwali Bonanza")
                .highestROI(new BigDecimal("2500.0"))
                .totalRedemptions(5400)
                .build();
    }

    // --- Sales Reports ---
    @Transactional(readOnly = true)
    public SalesSummary getSalesSummary(String period) {
        // Mock data logic for sales summary (similar to other reports)
        return SalesSummary.builder()
                .totalRevenue(new BigDecimal("12456890.00"))
                .totalOrders(245678)
                .averageOrderValue(new BigDecimal("500.00"))
                .totalDiscounts(new BigDecimal("150000.00"))
                .totalTax(new BigDecimal("225000.00"))
                .build();
    }

    // --- Inventory Reports ---
    @Transactional(readOnly = true)
    public Page<InventoryReportEntry> getInventoryReport(String search, Pageable pageable) {
        List<InventoryReportEntry> list = new ArrayList<>();
        list.add(InventoryReportEntry.builder()
                .productId("PRD-001").sku("GROC-WHT-001").productName("Whole Wheat Flour")
                .category("Groceries").qtyAvailable(150).qtyReserved(10)
                .unitCost(new BigDecimal("40.00")).stockValue(new BigDecimal("6000.00"))
                .status("IN_STOCK")
                .build());
        return new PageImpl<>(list, pageable, list.size());
    }

    @Transactional(readOnly = true)
    public InventorySummary getInventorySummary() {
        return InventorySummary.builder()
                .totalProducts(450)
                .lowStockItems(15)
                .outOfStockItems(5)
                .totalValuation(new BigDecimal("4500000.00"))
                .build();
    }

    // --- Export ---
    public byte[] exportReport(String reportType, String format) {
        // Return a mock CSV byte array representing an exported report
        String csvContent = "Id,Name,Value\n1,Test Report," + reportType;
        return csvContent.getBytes();
    }
}
