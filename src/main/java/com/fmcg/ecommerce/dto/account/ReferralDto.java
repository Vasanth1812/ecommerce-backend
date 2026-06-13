package com.fmcg.ecommerce.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class ReferralDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private String myReferralCode;
        private Integer totalLoyaltyPointsEarned;
        private List<ReferredFriend> friends;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReferredFriend {
        private String name;
        private String status; // PENDING or COMPLETED
        private Boolean rewardClaimed;
    }
}
