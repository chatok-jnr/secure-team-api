package com.chatokjunior.secureteamapi.auth.service;

import com.chatokjunior.secureteamapi.auth.dto.CreateUserRequest;
import com.chatokjunior.secureteamapi.auth.dto.CreateUserResponse;
import com.chatokjunior.secureteamapi.auth.dto.LoginUserRequest;
import com.chatokjunior.secureteamapi.exception.UserAlreadyExistsException;
import com.chatokjunior.secureteamapi.security.jwt.JwtService;
import com.chatokjunior.secureteamapi.user.entity.User;
import com.chatokjunior.secureteamapi.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

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
        String token = jwtService.generateAccessToken(username);

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

    public void logout(HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie(
                "accessToken",
                ""
        );

        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(15 * 60);

        response.addCookie(accessTokenCookie);
    }
}
