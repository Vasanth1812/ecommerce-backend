package com.fmcg.ecommerce.service;

import com.fmcg.ecommerce.dto.account.SettingsDto;

public interface SettingsService {
    SettingsDto.PreferenceResponse getPreferences(Long userId);
    SettingsDto.PreferenceResponse updatePreferences(Long userId, SettingsDto.PreferenceRequest request);
    void registerDeviceToken(Long userId, SettingsDto.DeviceTokenRequest request);
}
