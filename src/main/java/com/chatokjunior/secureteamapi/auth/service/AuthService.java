package com.chatokjunior.secureteamapi.auth.service;

import com.chatokjunior.secureteamapi.auth.dto.CreateUserRequest;
import com.chatokjunior.secureteamapi.auth.dto.CreateUserResponse;
import com.chatokjunior.secureteamapi.auth.dto.LoginUserRequest;
import com.chatokjunior.secureteamapi.exception.InvalidRefreshTokenException;
import com.chatokjunior.secureteamapi.exception.UserAlreadyExistsException;
import com.chatokjunior.secureteamapi.exception.UserNotFoundException;
import com.chatokjunior.secureteamapi.refresh.RefreshToken;
import com.chatokjunior.secureteamapi.refresh.RefreshTokenRepository;
import com.chatokjunior.secureteamapi.refresh.RefreshTokenService;
import com.chatokjunior.secureteamapi.security.jwt.JwtService;
import com.chatokjunior.secureteamapi.user.entity.User;
import com.chatokjunior.secureteamapi.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Ref;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

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
            HttpServletResponse response
    ) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String username = authentication.getName();
        addAccessToken(response, username);
        addRefreshToken(response, username);
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

        addAccessToken(response, refreshToken.getUser().getEmail());
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

    // =========================
    // Helper Functions
    // =========================

    private void addAccessToken(HttpServletResponse response, String username) {
        String token = jwtService.generateToken(username);

        Cookie accessTokenCookie = new Cookie(
                "accessToken",
                token
        );

        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(15 * 60);

        response.addCookie(accessTokenCookie);
    }

    private void addRefreshToken(HttpServletResponse response, String username) {

        User user = userRepository.findByEmail(username)
                        .orElseThrow(() -> new UserNotFoundException("User not found"));

       String refreshToken = refreshTokenService.createRefreshToken(user);

        Cookie refreshTokenCookie = new Cookie(
                "refreshToken",
                refreshToken
        );

        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(60 * 60 * 24 * 7);

        response.addCookie(refreshTokenCookie);
    }
}
