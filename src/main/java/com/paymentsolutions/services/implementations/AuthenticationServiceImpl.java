package com.paymentsolutions.services.implementations;


import com.paymentsolutions.dto.request.ChangePasswordRequest;
import com.paymentsolutions.dto.request.LoginRequest;
import com.paymentsolutions.dto.request.RegisterRequest;
import com.paymentsolutions.dto.response.AuthResponse;
import com.paymentsolutions.exception.ValidationException;
import com.paymentsolutions.model.Merchant;
import com.paymentsolutions.model.User;
import com.paymentsolutions.model.UserRole;
import com.paymentsolutions.repository.MerchantRepository;
import com.paymentsolutions.repository.UserRepository;
import com.paymentsolutions.security.JwtTokenProvider;
import com.paymentsolutions.services.IAuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of IAuthenticationService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthenticationServiceImpl implements IAuthenticationService {

    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new merchant: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already registered");
        }

        // Create merchant
        Merchant merchant = Merchant.builder()
                .businessName(request.getBusinessName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .businessType(request.getBusinessType())
                .taxId(request.getTaxId())
                .status("PENDING")
                .apiKey(generateApiKey())
                .build();

        merchant = merchantRepository.save(merchant);

        // Create user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(UserRole.MERCHANT)
                .merchantId(merchant.getId())
                .isActive(true)
                .build();

        user = userRepository.save(user);

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user);

        log.info("Merchant registered successfully: {}", merchant.getId());

        return buildAuthResponse(token, user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("User login attempt: {}", request.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = (User) authentication.getPrincipal();

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user);

        log.info("User logged in successfully: {}", user.getId());

        return buildAuthResponse(token, user);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        // Implementation for refresh token
        // This would validate the refresh token and generate new access token
        throw new UnsupportedOperationException("Refresh token not yet implemented");
    }

    @Override
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed for user: {}", userId);
    }

    @Override
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            // Generate reset token
            String resetToken = UUID.randomUUID().toString();
            // Store token (would typically use a separate table)
            // Send email with reset link
            log.info("Password reset requested for: {}", email);
        }
        // Always return success to prevent email enumeration
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        // Validate reset token
        // Update password
        throw new UnsupportedOperationException("Password reset not yet implemented");
    }

    @Override
    public void logout(UUID userId) {
        // Implement token blacklisting if needed
        log.info("User logged out: {}", userId);
    }

    // Helper methods

    private String generateApiKey() {
        return "sk_" + UUID.randomUUID().toString().replace("-", "");
    }

    private AuthResponse buildAuthResponse(String token, User user) {
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationTime())
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .merchantId(user.getMerchantId())
                .build();
    }
}
