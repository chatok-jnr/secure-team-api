package com.chatokjunior.secureteamapi.audit.services;

import com.chatokjunior.secureteamapi.audit.AuditLogRepository;
import com.chatokjunior.secureteamapi.audit.entity.AuditAction;
import com.chatokjunior.secureteamapi.audit.entity.AuditLog;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(
            AuditAction action,
            String actorEmail,
            String targetType,
            String targetId,
            String details,
            HttpServletRequest request
    ) {
        String ipAddress = null;
        String userAgent = null;

        if (request != null) {
            ipAddress = request.getRemoteAddr();
            userAgent = request.getHeader("User-Agent");
        }

        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .actorEmail(actorEmail)
                .targetType(targetType)
                .targetId(targetId)
                .details(details)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        auditLogRepository.save(auditLog);
    }
}
