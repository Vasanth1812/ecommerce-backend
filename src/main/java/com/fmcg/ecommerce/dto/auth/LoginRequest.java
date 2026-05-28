package com.fmcg.ecommerce.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Email or mobile is required")
    private String identifier; // email or mobile number

    @NotBlank(message = "Password is required")
    private String password;
}
