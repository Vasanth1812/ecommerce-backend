package com.fmcg.ecommerce.dto.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAnalyticsDto {
    private String clv;
    private String avgOrders;
    private String repeatRate;
    private String avgDays;
    private String churnRate;
    private String newCustomersMtd;
}
