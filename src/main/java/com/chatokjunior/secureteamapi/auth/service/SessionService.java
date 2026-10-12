package com.chatokjunior.secureteamapi.auth.service;

import com.chatokjunior.secureteamapi.auth.dto.SessionResponse;
import com.chatokjunior.secureteamapi.exception.UserNotFoundException;
import com.chatokjunior.secureteamapi.refresh.RefreshToken;
import com.chatokjunior.secureteamapi.refresh.RefreshTokenRepository;
import com.chatokjunior.secureteamapi.user.entity.User;
import com.chatokjunior.secureteamapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public List<SessionResponse> getSessions(String email) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        return refreshTokenRepository.findAllByUserAndRevokedFalseAndExpiresAtAfter(user, Instant.now())
                .stream()
                .map(this::toSessionResponse)
                .toList();
    }

    public void revokeSession(String email, UUID sessionId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        RefreshToken session = refreshTokenRepository
                .findByIdAndUser(sessionId, user)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Session not found"
                ));

        if (!session.isRevoked()) {
            session.setRevoked(true);
        }
    }

    // =========================
    // Helper Functions
    // =========================
    private SessionResponse toSessionResponse(RefreshToken token) {
        return SessionResponse.builder()
                .id(token.getId())
                .createdAt(token.getCreatedAt())
                .expiresAt(token.getExpiresAt())
                .build();
    }
}
