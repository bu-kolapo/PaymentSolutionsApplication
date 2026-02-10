package com.paymentsolutions.services;


import com.paymentsolutions.dto.request.ChangePasswordRequest;
import com.paymentsolutions.dto.request.LoginRequest;
import com.paymentsolutions.dto.request.RegisterRequest;
import com.paymentsolutions.dto.response.AuthResponse;

/**
 * Service interface for authentication and user management.
 * Handles registration, login, and password management.
 */
public interface IAuthenticationService {

    /**
     * Register a new merchant account
     *
     * @param request Registration details
     * @return AuthResponse with JWT token and user details
     * @throws ValidationException if email already exists
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticate a user and generate JWT token
     *
     * @param request Login credentials
     * @return AuthResponse with JWT token
     * @throws AuthenticationException if credentials are invalid
     */
    AuthResponse login(LoginRequest request);

    /**
     * Refresh an expired JWT token
     *
     * @param refreshToken The refresh token
     * @return New AuthResponse with refreshed token
     * @throws AuthenticationException if refresh token is invalid
     */
    AuthResponse refreshToken(String refreshToken) throws UnsupportedOperationException;

    /**
     * Change user password
     *
     * @param userId UUID of the user
     * @param request Password change details
     * @throws ValidationException if old password is incorrect
     */
    void changePassword(java.util.UUID userId, ChangePasswordRequest request);

    /**
     * Initiate password reset process
     *
     * @param email User's email address
     */
    void requestPasswordReset(String email);

    /**
     * Complete password reset with token
     *
     * @param token Reset token
     * @param newPassword New password
     * @throws ValidationException if token is invalid or expired
     */
    void resetPassword(String token, String newPassword);

    /**
     * Logout user (invalidate token)
     *
     * @param userId UUID of the user
     */
    void logout(java.util.UUID userId);
}
