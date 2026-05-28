package com.fmcg.ecommerce.dto.user;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String name;
    private String email;
    private String mobile;
}
