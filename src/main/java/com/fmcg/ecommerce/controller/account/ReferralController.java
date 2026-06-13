package com.fmcg.ecommerce.controller.account;

import com.fmcg.ecommerce.common.ApiResponse;
import com.fmcg.ecommerce.dto.account.ReferralDto;
import com.fmcg.ecommerce.entity.User;
import com.fmcg.ecommerce.repository.UserRepository;
import com.fmcg.ecommerce.service.ReferralService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/referrals")
@RequiredArgsConstructor
@Tag(name = "Customer - Referrals", description = "APIs for tracking invites and claiming loyalty points")
@SecurityRequirement(name = "bearerAuth")
public class ReferralController {

    private final ReferralService referralService;
    private final UserRepository userRepository;

    private Long getUserId(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    @GetMapping
    @Operation(summary = "Get your referral code and invite history")
    public ResponseEntity<ApiResponse<ReferralDto.Response>> getReferrals(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(referralService.getReferrals(getUserId(auth))));
    }

    @PostMapping("/claim")
    @Operation(summary = "Claim loyalty points for successful invites")
    public ResponseEntity<ApiResponse<ReferralDto.Response>> claimReward(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("Referral rewards successfully claimed", 
                referralService.claimReferralReward(getUserId(auth))));
    }
}
