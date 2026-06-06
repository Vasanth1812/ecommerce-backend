package com.fmcg.ecommerce.service.impl;

import com.fmcg.ecommerce.dto.auth.*;
import com.fmcg.ecommerce.dto.auth.LoginRequest;
import com.fmcg.ecommerce.dto.auth.RegisterRequest;
import com.fmcg.ecommerce.entity.AuthToken;
import com.fmcg.ecommerce.entity.LoyaltyAccount;
import com.fmcg.ecommerce.entity.OtpSession;
import com.fmcg.ecommerce.entity.User;
import com.fmcg.ecommerce.exception.BadRequestException;
import com.fmcg.ecommerce.exception.ResourceNotFoundException;
import com.fmcg.ecommerce.exception.UnauthorizedException;
import com.fmcg.ecommerce.repository.AuthTokenRepository;
import com.fmcg.ecommerce.repository.LoyaltyAccountRepository;
import com.fmcg.ecommerce.repository.OtpSessionRepository;
import com.fmcg.ecommerce.repository.UserRepository;
import com.fmcg.ecommerce.repository.DeliveryPartnerRepository;
import com.fmcg.ecommerce.entity.DeliveryPartner;
import com.fmcg.ecommerce.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl {

    private final UserRepository userRepository;
    private final OtpSessionRepository otpSessionRepository;
    private final AuthTokenRepository authTokenRepository;
    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final EmailServiceImpl emailService;

    // â”€â”€ Register (email + password) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("An account with this email already exists. Please login.");
        }
        if (request.getMobile() != null && !request.getMobile().isBlank()
                && userRepository.existsByMobile(request.getMobile().trim())) {
            throw new BadRequestException("An account with this mobile number already exists.");
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(email)
                .mobile(request.getMobile() != null ? request.getMobile().trim() : null)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null && !request.getRole().trim().isEmpty() ? request.getRole().trim().toUpperCase() : "CUSTOMER")
                .status("ACTIVE")
                .build();
        user = userRepository.save(user);

        // Create loyalty account for new user
        User finalUser = user;
        loyaltyAccountRepository.findByUserId(user.getId()).orElseGet(() -> {
            LoyaltyAccount loyalty = LoyaltyAccount.builder()
                    .user(finalUser)
                    .pointsBalance(0)
                    .tier("SILVER")
                    .tierUpdatedAt(LocalDateTime.now())
                    .build();
            return loyaltyAccountRepository.save(loyalty);
        });

        // Auto-create delivery partner profile if registered as a delivery boy
        if ("DELIVERY_BOY".equals(user.getRole())) {
            deliveryPartnerRepository.findByUserId(user.getId()).orElseGet(() -> {
                DeliveryPartner dp = DeliveryPartner.builder()
                        .user(finalUser)
                        .availabilityStatus("FREE")
                        .build();
                return deliveryPartnerRepository.save(dp);
            });
        }

        log.info("New user registered: {}", email);
        return generateTokensForUser(user);
    }

    // â”€â”€ Login (email/mobile + password) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getIdentifier().trim().toLowerCase();

        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByMobile(identifier))
                .orElseThrow(() -> new BadRequestException("Invalid email/mobile or password."));

        if (user.getPasswordHash() == null) {
            throw new BadRequestException(
                "This account was created with OTP login. Please use Send OTP to login.");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid email/mobile or password.");
        }
        if ("BLOCKED".equals(user.getStatus())) {
            throw new BadRequestException("Your account has been blocked. Please contact support.");
        }

        log.info("User logged in: {}", identifier);
        return generateTokensForUser(user);
    }

    // â”€â”€ Send OTP â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional
    public String sendOtp(SendOtpRequest request) {
        String identifier = request.getIdentifier().trim().toLowerCase();

        // Find or create user
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByMobile(identifier))
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(identifier.contains("@") ? identifier : null)
                            .mobile(!identifier.contains("@") ? identifier : null)
                            .name(request.getName() != null ? request.getName() : "User")
                            .role("CUSTOMER")
                            .status("ACTIVE")
                            .build();
                    return userRepository.save(newUser);
                });

        // Generate 6-digit OTP
        String otp = generateOtp();
        String otpHash = passwordEncoder.encode(otp);

        // Save OTP session (expire old ones first)
        OtpSession session = OtpSession.builder()
                .identifier(identifier)
                .otpHash(otpHash)
                .channel(request.getChannel() != null ? request.getChannel() : "EMAIL")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .attempts(0)
                .used(false)
                .build();
        otpSessionRepository.save(session);

        // Send OTP via email async
        sendOtpAsync(identifier, otp, user.getName());

        log.info("OTP sent to: {}", identifier);
        return "OTP sent successfully to " + maskIdentifier(identifier);
    }

    // â”€â”€ Verify OTP â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        String identifier = request.getIdentifier().trim().toLowerCase();

        OtpSession session = otpSessionRepository.findLatestByIdentifier(identifier)
                .orElseThrow(() -> new BadRequestException("No OTP found. Please request a new OTP."));

        // Check expiry
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }

        // Check attempts
        if (session.getAttempts() >= 3) {
            throw new BadRequestException("Too many failed attempts. Please request a new OTP.");
        }

        // Verify OTP
        if (!passwordEncoder.matches(request.getOtp(), session.getOtpHash())) {
            session.setAttempts(session.getAttempts() + 1);
            otpSessionRepository.save(session);
            int remaining = 3 - session.getAttempts();
            throw new BadRequestException("Invalid OTP. " + remaining + " attempt(s) remaining.");
        }

        // Mark OTP as used
        session.setUsed(true);
        otpSessionRepository.save(session);

        // Get user and activate
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByMobile(identifier))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }
        user.setStatus("ACTIVE");
        userRepository.save(user);

        // Create loyalty account if new user
        loyaltyAccountRepository.findByUserId(user.getId()).orElseGet(() -> {
            LoyaltyAccount loyalty = LoyaltyAccount.builder()
                    .user(user)
                    .pointsBalance(0)
                    .tier("SILVER")
                    .tierUpdatedAt(LocalDateTime.now())
                    .build();
            return loyaltyAccountRepository.save(loyalty);
        });

        // Auto-create delivery partner profile if registered as a delivery boy
        if ("DELIVERY_BOY".equals(user.getRole())) {
            deliveryPartnerRepository.findByUserId(user.getId()).orElseGet(() -> {
                DeliveryPartner dp = DeliveryPartner.builder()
                        .user(user)
                        .availabilityStatus("FREE")
                        .build();
                return deliveryPartnerRepository.save(dp);
            });
        }

        return generateTokensForUser(user);
    }

    // â”€â”€ Refresh Token â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        AuthToken storedToken = authTokenRepository
                .findByRefreshTokenAndRevokedFalse(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired refresh token"));

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            storedToken.setRevoked(true);
            authTokenRepository.save(storedToken);
            throw new UnauthorizedException("Refresh token expired. Please login again.");
        }

        // Revoke old token
        storedToken.setRevoked(true);
        authTokenRepository.save(storedToken);

        User user = storedToken.getUser();
        return generateTokensForUser(user);
    }

    // â”€â”€ Logout â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional
    public void logout(String refreshToken) {
        authTokenRepository.findByRefreshTokenAndRevokedFalse(refreshToken)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    authTokenRepository.save(token);
                });
    }

    // â”€â”€ Get Profile â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public UserSummaryDto getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .or(() -> userRepository.findByMobile(email))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toUserSummary(user);
    }

    // â”€â”€ Helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private AuthResponse generateTokensForUser(User user) {
        String accessToken = jwtUtil.generateAccessToken(
                user.getEmail() != null ? user.getEmail() : user.getMobile(),
                user.getRole()
        );
        String refreshToken = jwtUtil.generateRefreshToken(
                user.getEmail() != null ? user.getEmail() : user.getMobile()
        );

        AuthToken token = AuthToken.builder()
                .user(user)
                .refreshToken(refreshToken)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .revoked(false)
                .build();
        authTokenRepository.save(token);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(toUserSummary(user))
                .build();
    }

    private UserSummaryDto toUserSummary(User user) {
        return UserSummaryDto.builder()
                .id(user.getId())
                .publicId(user.getPublicId())
                .name(user.getName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    private String maskIdentifier(String identifier) {
        if (identifier.contains("@")) {
            int atIndex = identifier.indexOf('@');
            return identifier.substring(0, Math.min(3, atIndex)) + "***" + identifier.substring(atIndex);
        }
        return identifier.substring(0, 3) + "****" + identifier.substring(identifier.length() - 3);
    }

    @Async
    public void sendOtpAsync(String identifier, String otp, String name) {
        try {
            if (identifier.contains("@")) {
                emailService.sendOtpEmail(identifier, otp, name);
            }
        } catch (Exception e) {
            log.error("Failed to send OTP to {}: {}", identifier, e.getMessage());
        }
    }
}
