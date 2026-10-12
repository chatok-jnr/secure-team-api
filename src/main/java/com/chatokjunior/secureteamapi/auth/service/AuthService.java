package com.chatokjunior.secureteamapi.auth.service;

import com.chatokjunior.secureteamapi.auth.dto.*;
import com.chatokjunior.secureteamapi.auth.entity.PasswordResetToken;
import com.chatokjunior.secureteamapi.auth.repository.PasswordResetTokenRepository;
import com.chatokjunior.secureteamapi.exception.InvalidPasswordResetTokenException;
import com.chatokjunior.secureteamapi.exception.InvalidRefreshTokenException;
import com.chatokjunior.secureteamapi.exception.UserAlreadyExistsException;
import com.chatokjunior.secureteamapi.exception.UserNotFoundException;
import com.chatokjunior.secureteamapi.notification.EmailService;
import com.chatokjunior.secureteamapi.refresh.RefreshToken;
import com.chatokjunior.secureteamapi.refresh.RefreshTokenService;
import com.chatokjunior.secureteamapi.user.UserService;
import com.chatokjunior.secureteamapi.user.entity.User;
import com.chatokjunior.secureteamapi.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    @Value("${app.security.password-reset-token-ttl:30m}")
    private Duration resetTokenTtl;

    private final EmailService emailService;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final AuthSessionService authSessionService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Transactional
    public CreateUserResponse createUser(CreateUserRequest req) {

        boolean isExists = userRepository.existsByEmail(req.getEmail());

        if(isExists) {
            throw new UserAlreadyExistsException("User With this email already exists");
        }

        String hashedPassword = passwordEncoder.encode(req.getPassword());

        User user = User.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .password(hashedPassword)
                .build();

        User newUser = userRepository.save(user);

        return CreateUserResponse.builder()
                .id(newUser.getId())
                .fullName(newUser.getFullName())
                .email(newUser.getEmail())
                .role(newUser.getRole())
                .enabled(newUser.isEnabled())
                .build();
    }

    public void login(
            LoginUserRequest request,
            HttpServletResponse httpResponse
    ) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String username = authentication.getName();

        authSessionService.addAccessToken(httpResponse, username);
        authSessionService.addRefreshToken(httpResponse, username);
    }

    public void refresh(HttpServletRequest request, HttpServletResponse response) {

        Cookie[] cookies = request.getCookies();

        if(cookies == null) {
            throw new NullPointerException(
                    "Refresh token is missing"
            );
        }

        String rawToken = null;

        for(Cookie cookie: cookies) {
            if(cookie.getName().equals("refreshToken")) {
                rawToken = cookie.getValue();
            }
        }

        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidRefreshTokenException(
                    "Refresh token is missing, Please login again"
            );
        }

        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(rawToken);
        refreshTokenService.revokeRefreshToken(rawToken);

        authSessionService.addAccessToken(response, refreshToken.getUser().getEmail());
        authSessionService.addRefreshToken(response, refreshToken.getUser().getEmail());
    }

    public void logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        Cookie[] requestCookie = request.getCookies();

        if(requestCookie != null) {
            String refreshToken = null;

            for(Cookie cookie: requestCookie) {
                if(cookie.getName().equals("refreshToken")) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }

            if(refreshToken != null && !refreshToken.isBlank()) {
                refreshTokenService.revokeRefreshToken(refreshToken);
            }
        }


        clearTokens(response);
    }

    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("User with this email not found"));

        int isExist = passwordResetTokenRepository.getPasswordResetTokenByUserId(Instant.now(), user.getId());

        if(isExist > 0) return "A Password Resent Link is Sent to Your Email Address";

        String rawToken = generateToken();
        String tokenHash = hash(rawToken);

        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .tokenHash(tokenHash)
                .user(user)
                .expireAt(Instant.now().plus(resetTokenTtl))
                .build();

        passwordResetTokenRepository.save(passwordResetToken);

        String to = request.email();
        String subject = "Secure Team Api password reset request";
        String body = "If you are request to reset Secure Team Api password, please click on the link below\nhttp://localhost:8080/api/auth/reset/" + rawToken + "\nTeam unmbraCrown";

        emailService.sendMail(
                to,
                subject,
                body
        );

        return "A Password Resent Link is Sent to Your Email Address";
    }

    @Transactional
    public String resetPassword(
            HttpServletResponse httpResponse,
            ResetPasswordRequest request
    ) {

        String hashToken = hash(request.token());
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.getPasswordResetTokenByHashToken(hashToken, Instant.now())
                        .orElseThrow(()-> new InvalidPasswordResetTokenException("Invalid token, Please request for another one"));

        User user = passwordResetToken.getUser();

        String passwordHash = passwordEncoder.encode(request.newPassword());

        user.setPassword(passwordHash);
        userRepository.save(user);

        passwordResetToken.setUsed(true);
        passwordResetTokenRepository.save(passwordResetToken);

        userService.revokeAllSession(user);

        authSessionService.addAccessToken(httpResponse, user.getEmail());
        authSessionService.addRefreshToken(httpResponse, user.getEmail());

        String to = user.getEmail();
        String subject = "Password Changed";
        String body = "Hi,\n" + user.getFullName() + " You Secure Team Api password is just changed.\nIf you didn't do this then contact with the Secure-Team Support Center\nThank You";

        emailService.sendMail(
                to,
                subject,
                body
        );

        return "Password Updated Successfully";
    }

    @Transactional
    public void logoutAll(HttpServletResponse httpResponse) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails =  (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        userService.revokeAllSession(user);

        clearTokens(httpResponse);
    }

    // ===================================================
    // Helper Functions
    // ===================================================

    private static final int TOKEN_BYTES = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private void clearTokens(HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie("accessToken", "");

        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);

        response.addCookie(accessTokenCookie);


        Cookie refreshTokenCookie = new Cookie("refreshToken", "");

        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);

        response.addCookie(refreshTokenCookie);
    }
}
