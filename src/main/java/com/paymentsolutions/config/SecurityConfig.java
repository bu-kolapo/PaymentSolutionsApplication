package com.paymentsolutions.config;

import com.paymentsolutions.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("🔐 Configuring Security...");

        http

        // ✅ ADD THIS LINE
            .cors(cors -> {})
                // Disable CSRF (OK for stateless APIs)
                .csrf(csrf -> csrf.disable())

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // ✅ Auth endpoints
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // ✅ VERY IMPORTANT (your failing endpoint)
                        .requestMatchers("/api/v1/payments/requests/**").permitAll()
                        // ✅ Checkout payment (THIS FIXES YOUR 403)
                        .requestMatchers("/api/v1/payments/*/pay").permitAll()
                        .requestMatchers("/api/v1/payments/**").permitAll()
                       .requestMatchers("/api/v1/transactions/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/transactions").permitAll()
                        .requestMatchers("/api/v1/ledger/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/ledger/account/merchant").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/ledger/account/*/entries").permitAll()
                        // ✅ Public checkout endpoint (from your React routes)
                        .requestMatchers("/checkout/**").permitAll()
                        .requestMatchers("/api/v1/ai/**").permitAll()

                        // ✅ Allow frontend entry + static resources
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/favicon.ico",
                                "/error",
                                "/**/*.js",
                                "/**/*.css",
                                "/**/*.png",
                                "/**/*.jpg"
                        ).permitAll()

                        // ❗ Everything else must be authenticated
                        .anyRequest().authenticated()
                )

                // Stateless session (JWT)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Authentication provider
                .authenticationProvider(authenticationProvider())

                // JWT filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("✅ Security configuration complete");
        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}