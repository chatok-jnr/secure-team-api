package com.chatokjunior.secureteamapi.auth.service;

import com.chatokjunior.secureteamapi.exception.UserNotFoundException;
import com.chatokjunior.secureteamapi.refresh.RefreshTokenService;
import com.chatokjunior.secureteamapi.security.jwt.JwtService;
import com.chatokjunior.secureteamapi.user.entity.User;
import com.chatokjunior.secureteamapi.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthSessionService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    public void addAccessToken(HttpServletResponse response, String username) {
        String token = jwtService.generateToken(username);

        Cookie accessTokenCookie = new Cookie(
                "accessToken",
                token
        );

        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(15 * 60);
        accessTokenCookie.setAttribute("SameSite", "Strict");

        response.addCookie(accessTokenCookie);
    }

    public void addRefreshToken(HttpServletResponse response, String username) {

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
        refreshTokenCookie.setAttribute("SameSite", "Strict");

        response.addCookie(refreshTokenCookie);
    }
}
