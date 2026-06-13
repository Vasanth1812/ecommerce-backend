package com.fmcg.ecommerce.service.impl;

import com.fmcg.ecommerce.dto.account.ReferralDto;
import com.fmcg.ecommerce.entity.LoyaltyAccount;
import com.fmcg.ecommerce.entity.LoyaltyTransaction;
import com.fmcg.ecommerce.entity.Referral;
import com.fmcg.ecommerce.entity.User;
import com.fmcg.ecommerce.exception.BadRequestException;
import com.fmcg.ecommerce.exception.ResourceNotFoundException;
import com.fmcg.ecommerce.repository.LoyaltyAccountRepository;
import com.fmcg.ecommerce.repository.LoyaltyTransactionRepository;
import com.fmcg.ecommerce.repository.ReferralRepository;
import com.fmcg.ecommerce.repository.UserRepository;
import com.fmcg.ecommerce.service.ReferralService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReferralServiceImpl implements ReferralService {

    private final ReferralRepository referralRepository;
    private final UserRepository userRepository;
    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;

    @Override
    @Transactional
    public ReferralDto.Response getReferrals(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Generate referral code if user doesn't have one
        if (user.getReferralCode() == null) {
            String newCode = "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            user.setReferralCode(newCode);
            userRepository.save(user);
        }

        List<ReferralDto.ReferredFriend> friends = referralRepository.findByReferrerId(userId).stream()
                .map(r -> ReferralDto.ReferredFriend.builder()
                        .name(r.getReferred().getName())
                        .status(r.getStatus())
                        .rewardClaimed(r.getRewardClaimed())
                        .build())
                .collect(Collectors.toList());

        LoyaltyAccount loyaltyAccount = loyaltyAccountRepository.findByUserId(userId).orElse(null);
        int totalEarned = (loyaltyAccount != null) ? loyaltyAccount.getPointsBalance() : 0;

        return ReferralDto.Response.builder()
                .myReferralCode(user.getReferralCode())
                .totalLoyaltyPointsEarned(totalEarned)
                .friends(friends)
                .build();
    }

    @Override
    @Transactional
    public ReferralDto.Response claimReferralReward(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        List<Referral> unrewardedReferrals = referralRepository.findByReferrerId(userId).stream()
                .filter(r -> r.getStatus().equals("COMPLETED") && !r.getRewardClaimed())
                .collect(Collectors.toList());

        if (unrewardedReferrals.isEmpty()) {
            throw new BadRequestException("No new completed referrals available to claim.");
        }

        LoyaltyAccount account = loyaltyAccountRepository.findByUserId(userId)
                .orElseGet(() -> loyaltyAccountRepository.save(LoyaltyAccount.builder().user(user).build()));

        int totalRewardPoints = 0;

        for (Referral ref : unrewardedReferrals) {
            ref.setRewardClaimed(true);
            referralRepository.save(ref);
            
            // Standard reward is 500 points per successful referral
            totalRewardPoints += 500;
        }

        account.setPointsBalance(account.getPointsBalance() + totalRewardPoints);

        loyaltyAccountRepository.save(account);

        loyaltyTransactionRepository.save(LoyaltyTransaction.builder()
                .user(user)
                .action("EARN")
                .pointsEarned(totalRewardPoints)
                .description("Reward for successful referrals")
                .build());

        return getReferrals(userId);
    }
}
