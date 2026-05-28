package com.fmcg.ecommerce.controller;

import com.fmcg.ecommerce.common.ApiResponse;
import com.fmcg.ecommerce.dto.auth.*;
import com.fmcg.ecommerce.dto.auth.LoginRequest;
import com.fmcg.ecommerce.dto.auth.RegisterRequest;
import com.fmcg.ecommerce.service.impl.AuthServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "OTP-based login, token refresh, logout")
public class AuthController {

    private final AuthServiceImpl authService;

    // ── Register & Login (Email + Password) ──────────────────

    @PostMapping("/register")
    @Operation(summary = "Register a new customer account with email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.ok("Registration successful! Welcome aboard.", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email/mobile and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    // ── OTP-based Login ─────────────────────────────────────

    @PostMapping("/send-otp")
    @Operation(summary = "Send OTP to email or mobile")
    public ResponseEntity<ApiResponse<String>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        String message = authService.sendOtp(request);
        return ResponseEntity.ok(ApiResponse.ok(message));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP and get JWT tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.ok("Token refreshed", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and revoke refresh token")
    public ResponseEntity<ApiResponse<String>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully"));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile from JWT")
    public ResponseEntity<ApiResponse<UserSummaryDto>> getMe(Authentication authentication) {
        UserSummaryDto profile = authService.getProfile(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }
}
