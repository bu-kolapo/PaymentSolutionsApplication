package com.paymentsolutions.services.implementations;


import com.paymentsolutions.dto.request.LoginRequest;
import com.paymentsolutions.dto.request.RegisterRequest;
import com.paymentsolutions.dto.response.AuthResponse;
import com.paymentsolutions.dto.response.UserResponse;
import com.paymentsolutions.exception.PaymentException;
import com.paymentsolutions.model.Merchant;
import com.paymentsolutions.model.User;
import com.paymentsolutions.model.Role;
import com.paymentsolutions.repository.MerchantRepository;
import com.paymentsolutions.repository.UserRepository;
import com.paymentsolutions.security.JwtTokenProvider;
import com.paymentsolutions.services.IAuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        private final JwtTokenProvider jwtTokenProvider;
        private final AuthenticationManager authenticationManager;

        @Override
        @Transactional
        public AuthResponse register(RegisterRequest request) {
            log.info("Registering new user: {}", request.getEmail());

            if (userRepository.existsByEmail(request.getEmail())) {
                throw new PaymentException("Email already registered");
            }

            // Step 1: Create merchant first
            Merchant merchant = Merchant.builder()
                    .businessName(request.getFirstName() + " " + request.getLastName() + " Business")
                    .email(request.getEmail())
                    .status("ACTIVE")
                    .build();

            merchant = merchantRepository.save(merchant);
            log.info("✅ Merchant created: {}", merchant.getId());

            // Step 2: Create user with merchantId
            User user = User.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .merchantId(merchant.getId()) // ✅ Link to merchant
                    .role(Role.MERCHANT)
                    .enabled(true)
                    .build();

            user = userRepository.save(user);
            log.info("✅ User created with merchantId: {}", user.getMerchantId());

            // Generate token
            String token = jwtTokenProvider.generateToken(user);

            return AuthResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .expiresIn(86400L) // 24 hours
                    .user(UserResponse.builder()
                            .id(user.getId())
                            .email(user.getEmail())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .merchantId(user.getMerchantId()) // ✅ Include in response
                            .role(user.getRole().name())
                            .build())
                    .build();
        }

        @Override
        public AuthResponse login(LoginRequest request) {
            log.info("Login attempt: {}", request.getEmail());

            try {
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
                );

                User user = (User) authentication.getPrincipal();

                // ✅ Check if user has merchantId, create if missing (for existing users)
                if (user.getMerchantId() == null) {
                    log.warn("User {} has no merchantId, creating merchant...", user.getEmail());

                    Merchant merchant = Merchant.builder()
                            .businessName(user.getFirstName() + " " + user.getLastName() + " Business")
                            .email(user.getEmail())
                            .status("ACTIVE")
                            .build();

                    merchant = merchantRepository.save(merchant);

                    user.setMerchantId(merchant.getId());
                    userRepository.save(user);

                    log.info("✅ Merchant created for existing user: {}", merchant.getId());
                }

                String token = jwtTokenProvider.generateToken(user);

                return AuthResponse.builder()
                        .accessToken(token)
                        .tokenType("Bearer")
                        .expiresIn(86400L)
                        .user(UserResponse.builder()
                                .id(user.getId())
                                .email(user.getEmail())
                                .firstName(user.getFirstName())
                                .lastName(user.getLastName())
                                .merchantId(user.getMerchantId()) // ✅ Include in response
                                .role(user.getRole().name())
                                .build())
                        .build();

            } catch (BadCredentialsException e) {
                throw new PaymentException("Invalid email or password");
            }
        }
    }
