package com.fmcg.ecommerce.service.impl;

import com.fmcg.ecommerce.entity.AbTest;
import com.fmcg.ecommerce.entity.MarketingCampaign;
import com.fmcg.ecommerce.entity.Notification;
import com.fmcg.ecommerce.entity.Promotion;
import com.fmcg.ecommerce.entity.User;
import com.fmcg.ecommerce.exception.ResourceNotFoundException;
import com.fmcg.ecommerce.repository.AbTestRepository;
import com.fmcg.ecommerce.repository.MarketingCampaignRepository;
import com.fmcg.ecommerce.repository.NotificationRepository;
import com.fmcg.ecommerce.repository.PromotionRepository;
import com.fmcg.ecommerce.repository.UserRepository;
import com.fmcg.ecommerce.service.admin.AdminPromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminPromotionServiceImpl implements AdminPromotionService {

    private final PromotionRepository promotionRepository;
    private final MarketingCampaignRepository campaignRepository;
    private final NotificationRepository notificationRepository;
    private final AbTestRepository abTestRepository;
    private final UserRepository userRepository;

    @Override
    public List<Promotion> getFlashSales() {
        // Find all promotions that are of type FLASH_SALE
        return promotionRepository.findAll().stream()
                .filter(p -> "FLASH_SALE".equals(p.getType()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Promotion createFlashSale(Promotion promotion) {
        promotion.setType("FLASH_SALE");
        return promotionRepository.save(promotion);
    }

    @Override
    @Transactional
    public Promotion updateFlashSale(Long id, Promotion promotion) {
        Promotion existing = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion (Flash Sale)", id));
        existing.setName(promotion.getName());
        existing.setDescription(promotion.getDescription());
        existing.setDiscountValue(promotion.getDiscountValue());
        existing.setDiscountType(promotion.getDiscountType());
        existing.setStartDate(promotion.getStartDate());
        existing.setEndDate(promotion.getEndDate());
        existing.setStatus(promotion.getStatus());
        return promotionRepository.save(existing);
    }

    @Override
    public List<MarketingCampaign> getCampaigns() {
        return campaignRepository.findAll();
    }

    @Override
    @Transactional
    public MarketingCampaign createCampaign(MarketingCampaign campaign) {
        return campaignRepository.save(campaign);
    }

    @Override
    @Transactional
    public MarketingCampaign updateCampaign(Long id, MarketingCampaign campaign) {
        MarketingCampaign existing = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MarketingCampaign", id));
        existing.setName(campaign.getName());
        existing.setChannels(campaign.getChannels());
        existing.setBudget(campaign.getBudget());
        existing.setStatus(campaign.getStatus());
        existing.setStartDate(campaign.getStartDate());
        existing.setEndDate(campaign.getEndDate());
        return campaignRepository.save(existing);
    }

    @Override
    @Transactional
    public Notification sendPushNotification(Long userId, String title, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        
        Notification notif = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type("PROMOTION")
                .isRead(false)
                .build();
        return notificationRepository.save(notif);
    }

    @Override
    public List<AbTest> getAbTests() {
        return abTestRepository.findAll();
    }

    @Override
    @Transactional
    public AbTest createAbTest(AbTest test) {
        return abTestRepository.save(test);
    }
}
