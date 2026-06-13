package com.fmcg.ecommerce.service;

import com.fmcg.ecommerce.dto.account.ReferralDto;

public interface ReferralService {
    ReferralDto.Response getReferrals(Long userId);
    ReferralDto.Response claimReferralReward(Long userId);
}
