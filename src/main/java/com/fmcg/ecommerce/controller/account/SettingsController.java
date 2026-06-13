package com.fmcg.ecommerce.controller.account;

import com.fmcg.ecommerce.common.ApiResponse;
import com.fmcg.ecommerce.dto.account.SettingsDto;
import com.fmcg.ecommerce.entity.User;
import com.fmcg.ecommerce.repository.UserRepository;
import com.fmcg.ecommerce.service.SettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "Customer - Settings", description = "APIs for syncing UI preferences and registering push notification tokens")
@SecurityRequirement(name = "bearerAuth")
public class SettingsController {

    private final SettingsService settingsService;
    private final UserRepository userRepository;

    private Long getUserId(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    @GetMapping("/preferences")
    @Operation(summary = "Get user preferences")
    public ResponseEntity<ApiResponse<SettingsDto.PreferenceResponse>> getPreferences(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(settingsService.getPreferences(getUserId(auth))));
    }

    @PutMapping("/preferences")
    @Operation(summary = "Update user preferences")
    public ResponseEntity<ApiResponse<SettingsDto.PreferenceResponse>> updatePreferences(
            Authentication auth,
            @RequestBody SettingsDto.PreferenceRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Preferences synced across devices", 
                settingsService.updatePreferences(getUserId(auth), request)));
    }

    @PostMapping("/device-token")
    @Operation(summary = "Register device token for push notifications")
    public ResponseEntity<ApiResponse<String>> registerDeviceToken(
            Authentication auth,
            @RequestBody SettingsDto.DeviceTokenRequest request) {
        settingsService.registerDeviceToken(getUserId(auth), request);
        return ResponseEntity.ok(ApiResponse.ok("Device token registered successfully"));
    }
}
