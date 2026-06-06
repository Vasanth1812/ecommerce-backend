package com.fmcg.ecommerce.service.admin;

import com.fmcg.ecommerce.entity.AbTest;
import com.fmcg.ecommerce.entity.MarketingCampaign;
import com.fmcg.ecommerce.entity.Notification;
import com.fmcg.ecommerce.entity.Promotion;

import java.util.List;

public interface AdminPromotionService {
    // Flash Sales
    List<Promotion> getFlashSales();
    Promotion createFlashSale(Promotion promotion);
    Promotion updateFlashSale(Long id, Promotion promotion);

    // Campaigns
    List<MarketingCampaign> getCampaigns();
    MarketingCampaign createCampaign(MarketingCampaign campaign);
    MarketingCampaign updateCampaign(Long id, MarketingCampaign campaign);

    // Push Notifications
    Notification sendPushNotification(Long userId, String title, String message);
    
    // A/B Tests
    List<AbTest> getAbTests();
    AbTest createAbTest(AbTest test);
}
