package com.fmcg.ecommerce.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

public class SettingsDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PreferenceResponse {
        private String language;
        private String theme;
        private String currency;
        private Boolean emailNotifications;
        private Boolean pushNotifications;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "SettingsPreferenceRequest")
    public static class PreferenceRequest {
        private String language;
        private String theme;
        private String currency;
        private Boolean emailNotifications;
        private Boolean pushNotifications;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "SettingsDeviceTokenRequest")
    public static class DeviceTokenRequest {
        private String deviceToken;
        private String deviceType; // iOS, ANDROID, WEB
    }
}
