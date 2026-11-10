package com.example.facigen.service;

import com.example.facigen.Entity.PendingEmailChange;
import com.example.facigen.Entity.PendingRegistration;
import com.example.facigen.Entity.UserSession;
import com.example.facigen.dto.*;
import com.example.facigen.model.Role;
import com.example.facigen.Entity.User;
import com.example.facigen.repository.PendingEmailChangeRepository;
import com.example.facigen.repository.PendingRegistrationRepository;
import com.example.facigen.repository.UserRepository;
import com.example.facigen.repository.UserSessionRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class
AuthService {

    private final UserRepository repo;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final EmailService emailService;
    private final UserSessionRepository userSessionRepository;
    private final PendingEmailChangeRepository pendingEmailChangeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public RegisterResponse register(RegisterRequest request) {

        if (repo.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        if (request.getRole() == null || request.getRole().isBlank()) {
            throw new RuntimeException("Role is required");
        }

        Role role;
        LocalDateTime now = LocalDateTime.now();


        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "Invalid role. Use CUSTOMER or PROFESSIONAL"
            );
        }

        PendingRegistration pendingRegistration =
                pendingRegistrationRepository
                        .findByEmail(request.getEmail())
                        .orElseGet(PendingRegistration::new);

        boolean existingOtpStillValid =
                pendingRegistration.getId() != null
                        && pendingRegistration.getOtpExpiresAt() != null
                        && now.isBefore(pendingRegistration.getOtpExpiresAt())
                        && pendingRegistration.getFailedAttempts() < 5;

        if (existingOtpStillValid) {
            throw new RuntimeException(
                    "An OTP is already active. Please verify it or wait until it expires."
            );
        }


        String encodedPassword =
                passwordEncoder.encode(request.getPassword());


        SecureRandom secureRandom = new SecureRandom();

        String otp = String.format(
                "%06d",
                secureRandom.nextInt(1000000)
        );

        pendingRegistration.setName(request.getName());
        pendingRegistration.setEmail(request.getEmail());
        pendingRegistration.setPhone(request.getPhone());
        pendingRegistration.setPassword(encodedPassword);
        pendingRegistration.setRole(role);
        pendingRegistration.setOtp(passwordEncoder.encode(otp));

        pendingRegistration.setFailedAttempts(0);

        pendingRegistration.setCreatedAt(now);
        pendingRegistration.setOtpExpiresAt(now.plusMinutes(1));

        pendingRegistrationRepository.save(pendingRegistration);


        emailService.sendRegistrationOtpEmail(
                request.getEmail(),
                otp
        );

        return new RegisterResponse(
                "OTP sent to your email. Please verify your email."
        );
    }
    public RegisterResponse resendOtp(ResendOtpRequest request) {

        PendingRegistration pendingRegistration =
                pendingRegistrationRepository.findByEmail(request.getEmail())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Registration not found"
                                )
                        );

        LocalDateTime now = LocalDateTime.now();
        if (pendingRegistration.getCreatedAt()
                .plusSeconds(60)
                .isAfter(now)) {

            throw new RuntimeException(
                    "Please wait 60 seconds before requesting a new OTP"
            );
        }

        SecureRandom secureRandom = new SecureRandom();

        String otp = String.format(
                "%06d",
                secureRandom.nextInt(1000000)
        );


        pendingRegistration.setOtp(passwordEncoder.encode(otp));
        pendingRegistration.setFailedAttempts(0);
        pendingRegistration.setCreatedAt(now);
        pendingRegistration.setOtpExpiresAt(
                now.plusMinutes(1)
        );

        pendingRegistrationRepository.save(
                pendingRegistration
        );

        emailService.sendRegistrationOtpEmail(
                request.getEmail(),
                otp
        );

        return new RegisterResponse(
                "New OTP sent to your email. It is valid for 3 minutes."
        );
    }

    public class OtpVerificationException extends RuntimeException {

        public OtpVerificationException(String message) {
            super(message);
        }
    }

    @Transactional(noRollbackFor = OtpVerificationException.class)
    public RegisterResponse verifyRegistration(
            VerifyRegistrationRequest request
    ) {

        PendingRegistration pendingRegistration =
                pendingRegistrationRepository.findByEmail(request.getEmail())
                        .orElseThrow(() ->
                                new RuntimeException("Registration not found")
                        );

        // 1. Check OTP expiry
        if (!LocalDateTime.now().isBefore(
                pendingRegistration.getOtpExpiresAt()
        )) {

            throw new OtpVerificationException(
                    "OTP expired"
            );
        }

        // 2. Check maximum attempts
        if (pendingRegistration.getFailedAttempts() >= 5) {

            pendingRegistrationRepository.deleteByEmail(
                    request.getEmail()
            );

            throw new OtpVerificationException(
                    "Maximum OTP attempts exceeded. Please register again."
            );
        }

        // 3. Verify OTP against BCrypt hash
        if (!passwordEncoder.matches(
                request.getOtp(),
                pendingRegistration.getOtp()
        )) {

            int attempts = pendingRegistration.getFailedAttempts() + 1;

            // Fifth incorrect attempt
            if (attempts >= 5) {

                pendingRegistrationRepository.deleteByEmail(
                        request.getEmail()
                );

                throw new OtpVerificationException(
                        "Maximum OTP attempts exceeded. Please register again."
                );
            }

            // Save failed attempt
            pendingRegistration.setFailedAttempts(attempts);

            pendingRegistrationRepository.save(pendingRegistration);

            throw new OtpVerificationException(
                    "Invalid OTP. " + (5 - attempts) +
                            " attempts remaining."
            );
        }

        // 4. Create actual user after successful verification
        User user = new User();

        user.setName(pendingRegistration.getName());
        user.setEmail(pendingRegistration.getEmail());
        user.setPhone(pendingRegistration.getPhone());
        user.setPassword(pendingRegistration.getPassword());
        user.setRole(pendingRegistration.getRole());

        repo.save(user);

        // 5. Delete pending registration
        pendingRegistrationRepository.deleteByEmail(
                request.getEmail()
        );

        return new RegisterResponse(
                "Registration successful"
        );
    }


    @Transactional
    public LoginResponse login(LoginRequest request) {

        User user = repo.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password")
                );

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new RuntimeException(
                    "Invalid email or password"
            );
        }

        // Remove previous device session
        userSessionRepository.deleteByUserId(user.getId());
        userSessionRepository.flush();

        // Generate new session token
        String sessionToken = UUID.randomUUID().toString();

        LocalDateTime now = LocalDateTime.now();

        UserSession session = new UserSession();

        session.setUserId(user.getId());
        session.setSessionToken(sessionToken);
        session.setCreatedAt(now);
        session.setExpiresAt(now.plusYears(1));

        userSessionRepository.save(session);

        String jwtToken = jwtService.generateToken(
                user.getId(),
                user.getRole().name()
        );

        return new LoginResponse(
                "Login successful",
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getPhone(),
                jwtToken,
                sessionToken
        );
    }

    public RegisterResponse updateUser(
            Long userId,
            UpdateUserRequest request
    ) {

        User user = repo.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );


        boolean emailChanged =
                !user.getEmail().equalsIgnoreCase(request.getEmail());

        // Email has not changed → update name/phone directly
        if (!emailChanged) {
                user.setName(request.getName());

                user.setPhone(request.getPhone());
            repo.save(user);

            return new RegisterResponse(
                    "User updated1 successfully"
            );
        }

        // Email changed → check if new email already exists
        if (repo.existsByEmailAndIdNot(
                request.getEmail(),
                userId
        )) {
            throw new RuntimeException(
                    "Email already registered"
            );
        }

        // Find existing pending request
        PendingEmailChange existingRequest =
                pendingEmailChangeRepository.findByUserId(userId)
                        .orElse(null);

        LocalDateTime now = LocalDateTime.now();

        // 1-minute cooldown
        if (existingRequest != null &&
                existingRequest.getCreatedAt()
                        .plusSeconds(60)
                        .isAfter(now)) {

            throw new RuntimeException(
                    "Please wait 60 seconds before requesting a new OTP"
            );
        }

        // Generate OTP
        SecureRandom secureRandom = new SecureRandom();

        String otp = String.format(
                "%06d",
                secureRandom.nextInt(1000000)
        );

        // Create or update pending request
        PendingEmailChange pendingEmailChange =
                existingRequest != null
                        ? existingRequest
                        : new PendingEmailChange();

        pendingEmailChange.setUserId(userId);
        pendingEmailChange.setName(request.getName());
        pendingEmailChange.setNewEmail(request.getEmail());
        pendingEmailChange.setPhone(request.getPhone());
        pendingEmailChange.setOtp(otp);
        pendingEmailChange.setCreatedAt(now);
        pendingEmailChange.setOtpExpiresAt(
                now.plusMinutes(3)
        );

        pendingEmailChangeRepository.save(
                pendingEmailChange
        );

        // Send OTP
        emailService.sendRegistrationOtpEmail(
                request.getEmail(),
                otp
        );

        return new RegisterResponse(
                "OTP sent to your new email. Please verify to change your email."
        );
    }

    @Transactional
    public RegisterResponse verifyEmailChange(
            Long userId,
            VerifyEmailChangeRequest request
    ) {

        // Find the actual user
        User user = repo.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        // Find pending email-change request for this user
        PendingEmailChange pendingEmailChange =
                pendingEmailChangeRepository.findByUserId(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Email change request not found"
                                )
                        );

        // Check OTP expiry
        if (LocalDateTime.now().isAfter(
                pendingEmailChange.getOtpExpiresAt())) {

            throw new RuntimeException("OTP expired");
        }

        // Check OTP
        if (!pendingEmailChange.getOtp()
                .equals(request.getOtp())) {

            throw new RuntimeException("Invalid OTP");
        }

        // Check that the new email is still available
        if (repo.existsByEmailAndIdNot(
                pendingEmailChange.getNewEmail(),
                userId
        )) {

            throw new RuntimeException(
                    "Email already registered"
            );
        }

        // Update user details
        user.setName(pendingEmailChange.getName());
        user.setEmail(pendingEmailChange.getNewEmail());
        user.setPhone(pendingEmailChange.getPhone());

        repo.save(user);

        // OTP request is no longer needed
        pendingEmailChangeRepository.deleteByUserId(userId);

        return new RegisterResponse(
                "User updated successfully"
        );
    }

    public LoginResponse getUser(Long userId) {

        User user = repo.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        return new LoginResponse(
                "User fetched successfully",
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getPhone(),
                null,
                null
        );
    }

    public LoginResponse getCurrentUser(String authorization) {

            if (authorization == null ||
                    !authorization.startsWith("Bearer ")) {

                throw new RuntimeException("Invalid authorization token");
            }

            // Remove "Bearer "
            String jwtToken = authorization.substring(7);

            // Validate JWT
            if (!jwtService.isTokenValid(jwtToken)) {
                throw new RuntimeException("Invalid or expired JWT");
            }

            // Get user ID and role from JWT
            Long userId = jwtService.extractUserId(jwtToken);
            String jwtRole = jwtService.extractRole(jwtToken);

            // Get user from database
            User user = repo.findById(userId)
                    .orElseThrow(() ->
                            new RuntimeException("User not found")
                    );

            // Verify role
            if (!user.getRole().name().equals(jwtRole)) {
                throw new RuntimeException("Role mismatch");
            }

            return new LoginResponse(
                    "User fetched successfully",
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole(),
                    user.getPhone(),
                    jwtToken,
                    null
            );
        }

    public RegisterResponse logout(
            String accessToken,
            String refreshToken
    ) {

        // 1. Verify access JWT
        if (!jwtService.isTokenValid(accessToken)) {
            throw new RuntimeException("Invalid or expired JWT");
        }

        // 2. Get user from JWT
        Long userId = jwtService.extractUserId(accessToken);

        // 3. Find refresh-token session
        UserSession session =
                userSessionRepository
                        .findBySessionToken(refreshToken)
                        .orElseThrow(() ->
                                new RuntimeException("Session not found")
                        );

        // 4. Make sure refresh token belongs to same user
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("Invalid session");
        }

        // 5. Delete session
        userSessionRepository.delete(session);

        return new RegisterResponse(
                "Logged out successfully"
        );
    }

    @Transactional
    public  RefreshTokenHandler refresh(String sessionToken) {

        // Find session in database
        UserSession session =
                userSessionRepository
                        .findBySessionToken(sessionToken)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Session expired or invalid"
                                )
                        );

        // Check server-side session expiry
        if (LocalDateTime.now()
                .isAfter(session.getExpiresAt())) {

            userSessionRepository.delete(session);

            throw new RuntimeException(
                    "Session expired"
            );
        }

        // Get user from users table
        User user =
                repo.findById(session.getUserId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                )
                        );

        // Generate new JWT
        String jwtToken =
                jwtService.generateToken(
                        user.getId(),
                        user.getRole().name()
                );

        return new RefreshTokenHandler(
                jwtToken
        );
    }




}

