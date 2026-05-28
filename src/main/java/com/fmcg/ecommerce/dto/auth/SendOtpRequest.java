package com.fmcg.ecommerce.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendOtpRequest {
    @NotBlank(message = "Email or mobile is required")
    private String identifier;
    private String channel = "EMAIL";
    private String name;
}
