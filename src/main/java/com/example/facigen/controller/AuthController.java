package com.example.facigen.controller;

import com.example.facigen.dto.*;
import com.example.facigen.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/register")
    public RegisterResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return authService.register(request);
    }

    @PostMapping("/verify")
    public RegisterResponse verifyRegistration(
            @Valid @RequestBody VerifyRegistrationRequest request
    ) {
        return authService.verifyRegistration(request);
    }

    @PostMapping("/resend-otp")
    public RegisterResponse resendOtp(
            @Valid @RequestBody ResendOtpRequest request
    ) {
        return authService.resendOtp(request);
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }

    @PutMapping("/update/{userId}")
    public RegisterResponse updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return authService.updateUser(userId, request);
    }

    @PostMapping("/verify-email-change/{userId}")
    public RegisterResponse verifyEmailChange(
            @PathVariable Long userId,
            @Valid @RequestBody VerifyEmailChangeRequest request
    ) {
        return authService.verifyEmailChange(
                userId,
                request
        );
    }

    @GetMapping("/user/{userId}")
    public LoginResponse getUser(
            @PathVariable Long userId
    ) {
        return authService.getUser(userId);
    }

    @GetMapping("/me")
    public LoginResponse getCurrentUser(
            @RequestHeader("Authorization") String authorization
    ) {
        return authService.getCurrentUser(authorization);
    }

    @PostMapping("/logout")
    public RegisterResponse logout(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("Refresh-Token") String refreshToken
    ) {

        if (authorization == null ||
                !authorization.startsWith("Bearer ")) {

            throw new RuntimeException("Invalid authorization token");
        }

        String accessToken = authorization.substring(7);

        return authService.logout(accessToken, refreshToken);
    }

    @PostMapping("/refresh")
    public  RefreshTokenHandler refresh(
            @RequestHeader("Authorization") String authorization
    ) {
        if (authorization == null ||
                !authorization.startsWith("Bearer ")) {

            throw new RuntimeException(
                    "Invalid authorization token"
            );
        }

        String refreshToken =
                authorization.substring(7);

        return authService.refresh(refreshToken);
    }

}