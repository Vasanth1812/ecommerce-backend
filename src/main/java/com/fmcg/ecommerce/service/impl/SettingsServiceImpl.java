package com.fmcg.ecommerce.service.impl;

import com.fmcg.ecommerce.dto.account.SettingsDto;
import com.fmcg.ecommerce.entity.User;
import com.fmcg.ecommerce.entity.UserDeviceToken;
import com.fmcg.ecommerce.entity.UserPreference;
import com.fmcg.ecommerce.exception.ResourceNotFoundException;
import com.fmcg.ecommerce.repository.UserDeviceTokenRepository;
import com.fmcg.ecommerce.repository.UserPreferenceRepository;
import com.fmcg.ecommerce.repository.UserRepository;
import com.fmcg.ecommerce.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettingsServiceImpl implements SettingsService {

    private final UserPreferenceRepository userPreferenceRepository;
    private final UserDeviceTokenRepository userDeviceTokenRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public SettingsDto.PreferenceResponse getPreferences(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        UserPreference pref = userPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> userPreferenceRepository.save(UserPreference.builder().user(user).build()));

        return toResponse(pref);
    }

    @Override
    @Transactional
    public SettingsDto.PreferenceResponse updatePreferences(Long userId, SettingsDto.PreferenceRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        UserPreference pref = userPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> UserPreference.builder().user(user).build());

        pref.setLanguage(request.getLanguage() != null ? request.getLanguage() : pref.getLanguage());
        pref.setTheme(request.getTheme() != null ? request.getTheme() : pref.getTheme());
        pref.setCurrency(request.getCurrency() != null ? request.getCurrency() : pref.getCurrency());
        pref.setEmailNotifications(request.getEmailNotifications() != null ? request.getEmailNotifications() : pref.getEmailNotifications());
        pref.setPushNotifications(request.getPushNotifications() != null ? request.getPushNotifications() : pref.getPushNotifications());

        return toResponse(userPreferenceRepository.save(pref));
    }

    @Override
    @Transactional
    public void registerDeviceToken(Long userId, SettingsDto.DeviceTokenRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (userDeviceTokenRepository.findByUserIdAndDeviceToken(userId, request.getDeviceToken()).isEmpty()) {
            UserDeviceToken token = UserDeviceToken.builder()
                    .user(user)
                    .deviceToken(request.getDeviceToken())
                    .deviceType(request.getDeviceType())
                    .build();
            userDeviceTokenRepository.save(token);
        }
    }

    private SettingsDto.PreferenceResponse toResponse(UserPreference pref) {
        return SettingsDto.PreferenceResponse.builder()
                .language(pref.getLanguage())
                .theme(pref.getTheme())
                .currency(pref.getCurrency())
                .emailNotifications(pref.getEmailNotifications())
                .pushNotifications(pref.getPushNotifications())
                .build();
    }
}
