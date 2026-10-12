package com.chatokjunior.secureteamapi.refresh;

import com.chatokjunior.secureteamapi.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    List<RefreshToken> findAllByUser(User user);

    void deleteAllByUser(User user);

    List<RefreshToken> findAllByUserAndRevokedFalseAndExpiresAtAfter(User user, Instant now);
    Optional<RefreshToken> findByIdAndUser(UUID id, User user);
}
