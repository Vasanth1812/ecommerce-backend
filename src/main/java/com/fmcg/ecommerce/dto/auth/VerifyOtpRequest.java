package com.fmcg.ecommerce.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyOtpRequest {
    @NotBlank(message = "Identifier is required")
    private String identifier;
    @NotBlank(message = "OTP is required")
    private String otp;
    private String name;
}
