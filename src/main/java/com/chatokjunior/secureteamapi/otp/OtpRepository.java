package com.chatokjunior.secureteamapi.otp;

import com.chatokjunior.secureteamapi.otp.entity.Otp;
import com.chatokjunior.secureteamapi.otp.entity.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface OtpRepository extends JpaRepository<Otp, UUID> {
    @Query("""
    select o
    from Otp o
    where 
        o.email = :email 
        and o.type = :type 
        and o.usedAt is null 
        and o.expiresAt >= :now
""")
    Optional<Otp> findActiveOtp(String email, OtpType type, Instant now);

    @Query("""
    select count(o) > 0 from Otp o
    where 
        o.email = :email 
        and o.usedAt is null 
        and o.expiresAt >= :now
        and o.type = :type
""") boolean existActiveOtp(String email, Instant now, OtpType type);

}
