package com.chatokjunior.secureteamapi.otp;

import com.chatokjunior.secureteamapi.notification.EmailService;
import com.chatokjunior.secureteamapi.otp.entity.Otp;
import com.chatokjunior.secureteamapi.otp.entity.OtpType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class OtpService {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final long OTP_TTL_SECONDS = 900;
    private final OtpRepository otpRepository;
    private final EmailService emailService;
    private final SecretKeySpec hmacKey;

    public OtpService(
            OtpRepository otpRepository,
            @Value("${app.otp.secret}") String secret,
            EmailService emailService
    ) {
        this.otpRepository = otpRepository;
        this.hmacKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.emailService = emailService;
    }

    @Transactional
    public void createOtp(
            String email,
            OtpType type
    ) {
        String otp = generateOtp();
        String otpHash = hash(otp);

        Otp newOtp = Otp.builder()
                .email(email)
                .otpHash(otpHash)
                .type(type)
                .expiresAt(Instant.now().plusSeconds(OTP_TTL_SECONDS))
                .build();

        otpRepository.save(newOtp);

        String subject;
        String body;

        if(type.equals(OtpType.EMAIL_VERIFICATION)) {
            subject = "Account Activation OTP";
            body = "Your Secure-Team Account Activation otp is " + otp;
        } else {
            subject = "Two Factor Authentication OTP";
            body = "Your Two Factor Authentication OTP is " + otp;
        }

        emailService.sendMail(
                email,
                subject,
                body
        );
    }


    // ==========================
    // Helper Functions
    // ==========================

    private String generateOtp() {
        int otp = secureRandom.nextInt(1_000_000);
        return String.format("%06d", otp);
    }

    private String hash(String otp) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(hmacKey);
            return HexFormat.of().formatHex(mac.doFinal(otp.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to hash OTP", e);
        }
    }

    public boolean hashMatches(String otp, String storedHash) {
        return MessageDigest.isEqual(
                hash(otp).getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8));
    }

}
