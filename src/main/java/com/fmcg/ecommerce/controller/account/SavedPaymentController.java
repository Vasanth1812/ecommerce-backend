package com.fmcg.ecommerce.controller.account;

import com.fmcg.ecommerce.common.ApiResponse;
import com.fmcg.ecommerce.dto.account.SavedPaymentDto;
import com.fmcg.ecommerce.entity.User;
import com.fmcg.ecommerce.repository.UserRepository;
import com.fmcg.ecommerce.service.SavedPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user/payments")
@RequiredArgsConstructor
@Tag(name = "Customer - Saved Payments", description = "APIs for managing vaulted cards and UPI handles")
@SecurityRequirement(name = "bearerAuth")
public class SavedPaymentController {

    private final SavedPaymentService savedPaymentService;
    private final UserRepository userRepository;

    private Long getUserId(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    @GetMapping
    @Operation(summary = "Get all saved payment methods")
    public ResponseEntity<ApiResponse<List<SavedPaymentDto.Response>>> getSavedPayments(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(savedPaymentService.getSavedPayments(getUserId(auth))));
    }

    @PostMapping
    @Operation(summary = "Vault a new payment method")
    public ResponseEntity<ApiResponse<SavedPaymentDto.Response>> savePaymentMethod(
            Authentication auth,
            @RequestBody SavedPaymentDto.Request request) {
        return ResponseEntity.ok(ApiResponse.ok("Payment method saved securely", 
                savedPaymentService.savePaymentMethod(getUserId(auth), request)));
    }

    @DeleteMapping("/{paymentId}")
    @Operation(summary = "Remove a saved payment method")
    public ResponseEntity<ApiResponse<String>> deletePaymentMethod(
            Authentication auth,
            @PathVariable Long paymentId) {
        savedPaymentService.deletePaymentMethod(getUserId(auth), paymentId);
        return ResponseEntity.ok(ApiResponse.ok("Payment method removed successfully"));
    }
}
