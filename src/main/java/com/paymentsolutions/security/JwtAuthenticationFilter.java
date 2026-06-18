package com.paymentsolutions.security;

import com.paymentsolutions.model.User;
import com.paymentsolutions.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        boolean shouldSkip =
                path.startsWith("/api/v1/auth/") ||
                        path.startsWith("/checkout/") ||
                        path.equals("/") ||
                        path.equals("/index.html") ||
                        path.startsWith("/error") ||
                        path.startsWith("/favicon.ico") ||
                        path.matches(".*\\.(js|css|png|jpg|jpeg|gif|svg)$");

        if (shouldSkip) {
            log.debug("⏭️ Skipping JWT filter for: {} {}", request.getMethod(), path);
        }

        return shouldSkip;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        log.debug("🔵 JWT filter processing: {} {}", request.getMethod(), request.getRequestURI());

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = authHeader.substring(7);
            String userEmail = jwtTokenProvider.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                User user = userRepository.findByEmail(userEmail).orElse(null);

                if (user != null && jwtTokenProvider.isTokenValid(jwt, user)) {

                    // ✅ FIX: always assign explicit role (prevents 403 issues)
                    List<SimpleGrantedAuthority> authorities =
                            List.of(new SimpleGrantedAuthority("ROLE_USER"));

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    authorities
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("✅ JWT authentication successful for: {}", userEmail);
                } else {
                    log.debug("⚠️ Invalid JWT or user not found for: {}", userEmail);
                }
            }

        } catch (Exception e) {
            log.error("❌ JWT authentication error: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}