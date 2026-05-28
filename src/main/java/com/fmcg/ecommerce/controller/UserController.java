package com.fmcg.ecommerce.controller;

import com.fmcg.ecommerce.common.ApiResponse;
import com.fmcg.ecommerce.dto.auth.UserSummaryDto;
import com.fmcg.ecommerce.dto.user.AddressRequest;
import com.fmcg.ecommerce.dto.user.AddressResponse;
import com.fmcg.ecommerce.dto.user.UpdateProfileRequest;
import com.fmcg.ecommerce.service.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "User Profile", description = "Profile and address management")
public class UserController {

    private final UserServiceImpl userService;

    @GetMapping("/me/profile")
    @Operation(summary = "Get my profile")
    public ResponseEntity<ApiResponse<UserSummaryDto>> getProfile(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getProfile(auth.getName())));
    }

    @PatchMapping("/me/profile")
    @Operation(summary = "Update my profile")
    public ResponseEntity<ApiResponse<UserSummaryDto>> updateProfile(
            @RequestBody UpdateProfileRequest request, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("Profile updated", userService.updateProfile(auth.getName(), request)));
    }

    @GetMapping("/me/addresses")
    @Operation(summary = "Get my delivery addresses")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getAddresses(auth.getName())));
    }

    @PostMapping("/me/addresses")
    @Operation(summary = "Add new delivery address")
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @Valid @RequestBody AddressRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Address added", userService.addAddress(auth.getName(), request)));
    }

    @PutMapping("/me/addresses/{id}")
    @Operation(summary = "Update delivery address")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable Long id, @Valid @RequestBody AddressRequest request, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("Address updated", userService.updateAddress(auth.getName(), id, request)));
    }

    @DeleteMapping("/me/addresses/{id}")
    @Operation(summary = "Delete delivery address")
    public ResponseEntity<ApiResponse<String>> deleteAddress(
            @PathVariable Long id, Authentication auth) {
        userService.deleteAddress(auth.getName(), id);
        return ResponseEntity.ok(ApiResponse.ok("Address deleted"));
    }

    @PatchMapping("/me/addresses/{id}/set-default")
    @Operation(summary = "Set address as default")
    public ResponseEntity<ApiResponse<String>> setDefaultAddress(
            @PathVariable Long id, Authentication auth) {
        userService.setDefaultAddress(auth.getName(), id);
        return ResponseEntity.ok(ApiResponse.ok("Default address updated"));
    }
}
