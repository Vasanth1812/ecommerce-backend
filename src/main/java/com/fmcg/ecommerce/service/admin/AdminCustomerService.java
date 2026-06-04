package com.fmcg.ecommerce.service.admin;

import com.fmcg.ecommerce.dto.customer.CustomerAnalyticsDto;
import com.fmcg.ecommerce.dto.customer.SupportTicketDto;
import com.fmcg.ecommerce.entity.CustomerSegment;
import com.fmcg.ecommerce.entity.User;

import java.util.List;

public interface AdminCustomerService {
    // Segments
    List<CustomerSegment> getAllSegments();
    CustomerSegment createSegment(CustomerSegment segment);
    CustomerSegment updateSegment(Long id, CustomerSegment segment);

    // Analytics
    CustomerAnalyticsDto getCustomerAnalytics();

    // Tickets
    List<SupportTicketDto> getAllTickets();
    SupportTicketDto createTicket(SupportTicketDto dto);
    SupportTicketDto updateTicketStatus(Long id, String status);

    // Fraud
    List<User> getHighRiskUsers();
    User resetFraudScore(Long userId);
}
