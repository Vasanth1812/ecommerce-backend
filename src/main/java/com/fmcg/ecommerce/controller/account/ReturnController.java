package com.fmcg.ecommerce.controller.account;

import com.fmcg.ecommerce.common.ApiResponse;
import com.fmcg.ecommerce.dto.account.ReturnDto;
import com.fmcg.ecommerce.entity.User;
import com.fmcg.ecommerce.repository.UserRepository;
import com.fmcg.ecommerce.service.ReturnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders/returns")
@RequiredArgsConstructor
@Tag(name = "Customer - Returns", description = "APIs for managing order return requests and refunds")
@SecurityRequirement(name = "bearerAuth")
public class ReturnController {

    private final ReturnService returnService;
    private final UserRepository userRepository;

    private Long getUserId(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    @GetMapping
    @Operation(summary = "Get user's return history")
    public ResponseEntity<ApiResponse<List<ReturnDto.Response>>> getReturns(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(returnService.getReturns(getUserId(auth))));
    }

    @PostMapping
    @Operation(summary = "Submit a new return request")
    public ResponseEntity<ApiResponse<ReturnDto.Response>> submitReturn(
            Authentication auth,
            @RequestBody ReturnDto.Request request) {
        return ResponseEntity.ok(ApiResponse.ok("Return request submitted successfully", 
                returnService.submitReturn(getUserId(auth), request)));
    }
}
