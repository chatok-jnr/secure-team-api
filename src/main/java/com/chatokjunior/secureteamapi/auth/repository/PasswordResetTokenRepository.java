package com.chatokjunior.secureteamapi.auth.repository;

import com.chatokjunior.secureteamapi.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    @Query("""
    select count(prt.id)
    from PasswordResetToken prt
    where 
        prt.isUsed = false
        and prt.user.id = :userId
        and prt.expireAt >= :now
""") int getPasswordResetTokenByUserId(Instant now, UUID userId);
}
